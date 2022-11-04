package dev.compactmods.machines.machine.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.room.graph.RoomReferenceNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores information on how external machines connect to the rooms in the compact machine
 * dimension. Per-dimension since 4.3.0.
 */
public class DimensionMachineGraph extends SavedData {

    private static final Logger LOG = LoggingUtil.modLog();

    private final ResourceKey<Level> level;
    private final MutableValueGraph<IGraphNode, IGraphEdge> graph;
    private final Map<BlockPos, CompactMachineNode> machines;
    private final Map<String, RoomReferenceNode> rooms;

    public static final String DATA_KEY = "machine_connections";
    private final Codec<List<CompactMachineConnectionInfo>> CONN_CODEC = CompactMachineConnectionInfo.CODEC
            .listOf()
            .fieldOf("connections")
            .codec();

    private DimensionMachineGraph(ResourceKey<Level> level) {
        this.level = level;
        graph = ValueGraphBuilder
                .directed()
                .build();

        machines = new HashMap<>();
        rooms = new HashMap<>();
    }

    public DimensionMachineGraph(ResourceKey<Level> level, @NotNull CompoundTag nbt) {
        this(level);

        if (nbt.contains("graph")) {
            CompoundTag graphNbt = nbt.getCompound("graph");

            final var connectionData = CONN_CODEC.parse(NbtOps.INSTANCE, graphNbt)
                    .resultOrPartial(LOG::error)
                    .orElseThrow();

            loadConnections(connectionData);
        }
    }

    private void loadConnections(List<CompactMachineConnectionInfo> connectionInfo) {
        for (CompactMachineConnectionInfo i : connectionInfo) {
            addRoom(i.roomCode);
            for (var connectedMachine : i.machines()) {
                addMachine(connectedMachine);
                connectMachineToRoom(connectedMachine, i.roomCode);
            }
        }
    }

    public static DimensionMachineGraph forDimension(ServerLevel dimension) {
        final var dimStore = dimension.getDataStorage();
        return dimStore.computeIfAbsent(tag -> new DimensionMachineGraph(dimension.dimension(), tag),
                () -> new DimensionMachineGraph(dimension.dimension()), DATA_KEY);
    }

    private List<CompactMachineConnectionInfo> buildConnections() {
        List<CompactMachineConnectionInfo> result = new ArrayList<>();
        this.rooms.forEach((roomCode, node) -> {
            final var machines = this.getMachinesFor(roomCode);
            CompactMachineConnectionInfo roomInfo = new CompactMachineConnectionInfo(roomCode, ImmutableList.copyOf(machines));
            result.add(roomInfo);
        });

        return result;
    }

    public void addMachine(BlockPos machine) {
        if (this.machines.containsKey(machine))
            return;

        CompactMachineNode node = new CompactMachineNode(this.level, machine);
        graph.addNode(node);
        machines.put(machine, node);


        this.setDirty();
    }

    public void addRoom(String roomCode) {
        if (this.rooms.containsKey(roomCode))
            return;

        var node = new RoomReferenceNode(roomCode);
        graph.addNode(node);
        rooms.put(roomCode, node);

        this.setDirty();
    }

    public void connectMachineToRoom(BlockPos machine, String room) {
        if (!machines.containsKey(machine))
            addMachine(machine);

        if (!rooms.containsKey(room))
            addRoom(room);

        var machineNode = machines.get(machine);
        var roomNode = rooms.get(room);

        graph.putEdgeValue(machineNode, roomNode, new MachineRoomEdge());

        this.setDirty();
    }

    public Set<BlockPos> getMachinesFor(String room) {
        if(!rooms.containsKey(room))
            return Collections.emptySet();

        var node = this.rooms.get(room);
        var inbound = graph.predecessors(node);

        return inbound.stream()
                .filter(CompactMachineNode.class::isInstance)
                .map(CompactMachineNode.class::cast)
                .map(CompactMachineNode::position)
                .collect(Collectors.toSet());
    }

    public Optional<String> getConnectedRoom(BlockPos machinePos) {
        if (!this.machines.containsKey(machinePos))
            return Optional.empty();

        var node = this.machines.get(machinePos);
        var connected = this.graph.successors(node);
        return connected.stream()
                .filter(n -> n instanceof RoomReferenceNode)
                .map(n -> (RoomReferenceNode) n)
                .map(RoomReferenceNode::code)
                .findFirst();
    }

    public Stream<CompactMachineNode> getMachines() {
        return this.machines.values().stream();
    }

    public void disconnectAndUnregister(int machine) {
        if (!machines.containsKey(machine))
            return;

        final var node = machines.get(machine);
        graph.removeNode(node);
        machines.remove(machine);
    }

    public void removeRoom(String room) {
        if (!this.rooms.containsKey(room))
            return;

        graph.removeNode(rooms.get(room));
        rooms.remove(room);
    }

    public void disconnect(BlockPos machine) {
        if (!machines.containsKey(machine))
            return;

        final var node = machines.get(machine);
        graph.successors(node).stream()
                .filter(cn -> cn instanceof RoomReferenceNode)
                .forEach(room -> graph.removeEdge(node, room));

        setDirty();
    }

    public Optional<CompactMachineNode> getMachineNode(BlockPos worldPosition) {
        return Optional.ofNullable(machines.get(worldPosition));
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag nbt) {
        final var connData = buildConnections();

        CONN_CODEC.encodeStart(NbtOps.INSTANCE, connData)
                .resultOrPartial(LOG::error)
                .ifPresent(gNbt -> nbt.put("graph", gNbt));

        return nbt;
    }

    /**
     * Data structure for serialization. Do not use directly.
     */
    private record CompactMachineConnectionInfo(String roomCode, List<BlockPos> machines) {
        public static final Codec<CompactMachineConnectionInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING
                        .fieldOf("room")
                        .forGetter(CompactMachineConnectionInfo::roomCode),

                BlockPos.CODEC.listOf()
                        .fieldOf("machines")
                        .forGetter(CompactMachineConnectionInfo::machines)
        ).apply(i, CompactMachineConnectionInfo::new));

    }
}
