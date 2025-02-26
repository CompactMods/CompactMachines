package dev.compactmods.machines.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.MemoryGraph;
import dev.compactmods.feather.edge.impl.EmptyEdge;
import dev.compactmods.feather.node.Node;
import dev.compactmods.feather.traversal.GraphNodeTransformationFunction;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.api.room.history.IPlayerEntryPointHistoryManager;
import dev.compactmods.machines.api.room.history.PlayerRoomHistoryEntry;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.room.history.RoomEntryResult;
import dev.compactmods.machines.data.CodecHolder;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.room.graph.node.RoomReferenceNode;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlayerEntryPointHistoryManager implements CodecHolder<PlayerEntryPointHistoryManager>, CMDataFile, IPlayerEntryPointHistoryManager {

    private static final Logger LOGS = LogManager.getLogger();

    public static final Codec<PlayerEntryPointHistoryManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.fieldOf("max_depth").forGetter(x -> x.maxDepth),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, PlayerRoomHistoryEntry.CODEC.listOf())
                            .fieldOf("history")
                            .forGetter(PlayerEntryPointHistoryManager::playerRoomHistory)
    ).apply(inst, PlayerEntryPointHistoryManager::new));

    private final MemoryGraph graph;
    private final int maxDepth;

    private final HashMap<String, RoomReferenceNode> roomNodes;
    private final HashMap<UUID, PlayerReferenceNode> playerNodes;
    private final HashMap<UUID, PlayerEntryPointNode> latestEntryPoints;

    private static final GraphNodeTransformationFunction<PlayerReferenceNode, Stream<PlayerRoomHistoryEntry>> PLAYER_TO_HISTORY =
            (graph, input) -> graph.outboundEdges(input, PlayerEntryPointNode.class)
                    .flatMap(e -> graph.outboundEdges(e.target().get(), RoomReferenceNode.class, PlayerRoomEntryEdge.class))
                    .map(PlayerEntryPointHistoryManager::fromEdge)
                    .sorted(Comparator.comparing(PlayerRoomHistoryEntry::instant).reversed());

    private static final GraphNodeTransformationFunction<PlayerReferenceNode, Stream<PlayerEntryPointNode>> PLAYER_TO_HISTORY_NODES =
            (graph, input) -> graph.outboundEdges(input, PlayerEntryPointNode.class)
                    .flatMap(e -> graph.outboundEdges(e.target().get(), RoomReferenceNode.class, PlayerRoomEntryEdge.class))
                    .sorted(Comparator.comparing(PlayerRoomEntryEdge::entryTime).reversed())
                    .map(e -> e.source().get());

    public PlayerEntryPointHistoryManager(int maxDepth) {
        this(maxDepth, Collections.emptyMap());
    }

    private PlayerEntryPointHistoryManager(int maxDepth, Map<UUID, List<PlayerRoomHistoryEntry>> history) {
        this.graph = new MemoryGraph();
        this.maxDepth = maxDepth;
        this.roomNodes = new HashMap<>();
        this.playerNodes = new HashMap<>();
        this.latestEntryPoints = new HashMap<>();

        for(var entry : history.entrySet()) {
            LOGS.debug("Loading history for player: " + entry.getKey());
            entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(PlayerRoomHistoryEntry::instant))
                    .forEach(hist -> addRoomEntryUnsafe(entry.getKey(), hist));
        }
    }

    @Override
    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.PLAYER_SPAWNS.apply(server);
    }

    private static PlayerRoomHistoryEntry fromEdge(PlayerRoomEntryEdge edge) {
        return new PlayerRoomHistoryEntry(edge.target().get().code(), edge.entryTime(), edge.source().get().data());
    }

    public void popHistory(Player player, int steps) {
        final var playerNode = playerNodes.get(player.getUUID());
        if (playerNode == null) return;

        var historyNodes = graph.transformFunc(PLAYER_TO_HISTORY_NODES, playerNode)
                .limit(steps)
                .collect(Collectors.toSet());

        historyNodes.forEach(graph::removeNode);

        final var newLatest = graph.transformFunc(PLAYER_TO_HISTORY_NODES, playerNode).findFirst();
        newLatest.ifPresentOrElse(
                l -> latestEntryPoints.replace(player.getUUID(), l),
                () -> latestEntryPoints.remove(player.getUUID()));
    }

    public Optional<PlayerRoomHistoryEntry> lastHistory(Player player) {
        final var lastEntry = latestEntryPoints.get(player.getUUID());
        if(lastEntry == null) return Optional.empty();
        return graph.outboundEdges(lastEntry, RoomReferenceNode.class, PlayerRoomEntryEdge.class)
                .max(Comparator.comparing(PlayerRoomEntryEdge::entryTime))
                .map(PlayerEntryPointHistoryManager::fromEdge);
    }

    public Stream<PlayerRoomHistoryEntry> history(Player player) {
        return history(player.getUUID());
    }

    public Stream<PlayerRoomHistoryEntry> history(UUID player) {
        final var playerNode = playerNodes.get(player);
        if (playerNode == null)
            return Stream.empty();

        return graph.transformFunc(PLAYER_TO_HISTORY, playerNode);
    }

    private Map<UUID, List<PlayerRoomHistoryEntry>> playerRoomHistory() {
        return playerNodes.keySet()
                .stream()
                .collect(Collectors.toMap(pid -> pid, pid -> history(pid).toList()));
    }

    public RoomEntryResult enterRoom(UUID player, PlayerRoomHistoryEntry history) {
        return addRoomEntryUnsafe(player, history);
    }

    private @NotNull RoomEntryResult addRoomEntryUnsafe(UUID player, PlayerRoomHistoryEntry history) {
        PlayerReferenceNode playerNode = getOrCreatePlayer(player);

        long depth = graph.outboundEdges(playerNode, PlayerEntryPointNode.class).count();
        if (depth >= maxDepth)
            return RoomEntryResult.FAILED_TOO_FAR_DOWN;

        RoomReferenceNode roomNode = getOrCreateRoom(history.roomCode());

        PlayerEntryPointNode entryNode = new PlayerEntryPointNode(UUID.randomUUID(), history.entryPoint());
        if (latestEntryPoints.containsKey(player)) {
            final var prev = latestEntryPoints.replace(player, entryNode);
            if (prev != null)
                graph.connectNodes(prev, entryNode, new EmptyEdge<>(prev, entryNode));
        } else {
            latestEntryPoints.put(player, entryNode);
        }

        // PlayerReferenceNode ---> RoomEntryPointNode
        // RoomEntryPointNode -[PlayerRoomEntryEdge]-> RoomReferenceNode
        graph.connectNodes(playerNode, entryNode, new EmptyEdge<>(playerNode, entryNode));
        graph.connectNodes(entryNode, roomNode, new PlayerRoomEntryEdge(entryNode, roomNode, history.instant()));

        return RoomEntryResult.SUCCESS;
    }

    public RoomEntryResult enterRoom(Player player, String roomCode, RoomEntryPoint entryPoint) {
        return enterRoom(player.getUUID(), new PlayerRoomHistoryEntry(roomCode, Instant.now(), entryPoint));
    }

    @NotNull
    private RoomReferenceNode getOrCreateRoom(String roomCode) {
        return roomNodes.computeIfAbsent(roomCode, (code) -> {
            var node = new RoomReferenceNode(roomCode);
            graph.addNode(node);
            return node;
        });
    }

    @NotNull
    private PlayerReferenceNode getOrCreatePlayer(UUID player) {
        return playerNodes.computeIfAbsent(player, (o) -> {
            var node = new PlayerReferenceNode(UUID.randomUUID(), player);
            graph.addNode(node);
            return node;
        });
    }

    public void clearHistory(ServerPlayer player) {
        if (!playerNodes.containsKey(player.getUUID()))
            return;

        final var playerNode = playerNodes.get(player.getUUID());
        final var list = graph.successors(playerNode)
                .filter(PlayerEntryPointNode.class::isInstance)
                .toList();

        for (Node<?> node : list) {
            graph.removeNode(node);
        }

        latestEntryPoints.remove(player.getUUID());
    }

    @Override
    public Codec<PlayerEntryPointHistoryManager> codec() {
        return CODEC;
    }
}
