package dev.compactmods.machines.machine.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores information on how external machines connect to the rooms in the compact machine
 * dimension. Per-dimension since 4.3.0.
 */
public class DimensionMachineGraph extends SavedData {

    private final ResourceKey<Level> level;
    private final MutableValueGraph<IGraphNodeType, IGraphEdge> graph;
    private final Map<BlockPos, CompactMachineNode> machines;
    private final Map<ChunkPos, CompactMachineRoomNode> rooms;

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

    private DimensionMachineGraph(ResourceKey<Level> level, @Nonnull CompoundTag nbt) {
        this(level);

        if (nbt.contains("graph")) {
            CompoundTag graphNbt = nbt.getCompound("graph");

            final var connectionData = CONN_CODEC.parse(NbtOps.INSTANCE, graphNbt)
                    .resultOrPartial(CompactMachines.LOGGER::error)
                    .orElseThrow();

            loadConnections(connectionData);
        }
    }

    private void loadConnections(List<CompactMachineConnectionInfo> connectionInfo) {
        for (CompactMachineConnectionInfo i : connectionInfo) {
            addRoom(i.roomChunk);
            for (var connectedMachine : i.machines()) {
                addMachine(connectedMachine);
                connectMachineToRoom(connectedMachine, i.roomChunk);
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
        this.rooms.forEach((chunk, node) -> {
            Collection<BlockPos> machines = this.getMachinesFor(chunk);
            CompactMachineConnectionInfo roomInfo = new CompactMachineConnectionInfo(chunk, machines);
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

    public void addRoom(ChunkPos roomChunk) {
        if (this.rooms.containsKey(roomChunk))
            return;

        CompactMachineRoomNode node = new CompactMachineRoomNode(roomChunk);
        graph.addNode(node);
        rooms.put(roomChunk, node);

        this.setDirty();
    }

    public void connectMachineToRoom(BlockPos machine, ChunkPos room) {
        if (!machines.containsKey(machine))
            addMachine(machine);

        if (!rooms.containsKey(room))
            addRoom(room);

        CompactMachineNode machineNode = machines.get(machine);
        CompactMachineRoomNode roomNode = rooms.get(room);

        graph.putEdgeValue(machineNode, roomNode, new MachineRoomEdge());

        this.setDirty();
    }

    public Collection<BlockPos> getMachinesFor(ChunkPos room) {
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

    public Optional<ChunkPos> getConnectedRoom(BlockPos machinePos) {
        if (!this.machines.containsKey(machinePos))
            return Optional.empty();

        var node = this.machines.get(machinePos);
        var connected = this.graph.successors(node);
        return connected.stream()
                .filter(n -> n instanceof CompactMachineRoomNode)
                .map(n -> (CompactMachineRoomNode) n)
                .map(CompactMachineRoomNode::pos)
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

    public void removeRoom(ChunkPos room) {
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
                .filter(cn -> cn instanceof CompactMachineRoomNode)
                .forEach(room -> graph.removeEdge(node, room));
    }

    public Optional<CompactMachineNode> getMachineNode(BlockPos worldPosition) {
        return Optional.ofNullable(machines.get(worldPosition));
    }

    public Optional<CompactMachineRoomNode> getRoomNode(ChunkPos room) {
        return Optional.ofNullable(rooms.get(room));
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        final var connData = buildConnections();

        CONN_CODEC.encodeStart(NbtOps.INSTANCE, connData)
                .resultOrPartial(CompactMachines.LOGGER::error)
                .ifPresent(gNbt -> nbt.put("graph", gNbt));

        return nbt;
    }

    /**
     * Data structure for serialization. Do not use directly.
     */
    private static class CompactMachineConnectionInfo {
        private final ChunkPos roomChunk;
        private final List<BlockPos> connectedMachines;

        public static final Codec<CompactMachineConnectionInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                CodecExtensions.CHUNKPOS
                        .fieldOf("room")
                        .forGetter(CompactMachineConnectionInfo::room),

                BlockPos.CODEC.listOf()
                        .fieldOf("machines")
                        .forGetter(CompactMachineConnectionInfo::machines)
        ).apply(i, CompactMachineConnectionInfo::new));

        public CompactMachineConnectionInfo(ChunkPos roomChunk, Collection<BlockPos> connections) {
            this.roomChunk = roomChunk;
            this.connectedMachines = ImmutableList.copyOf(connections);
        }

        public ChunkPos room() {
            return this.roomChunk;
        }

        public List<BlockPos> machines() {
            return this.connectedMachines;
        }
    }
}
