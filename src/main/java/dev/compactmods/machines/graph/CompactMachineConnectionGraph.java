package dev.compactmods.machines.graph;

import com.google.common.collect.ImmutableList;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import net.minecraft.world.level.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores information on how external machines connect to the rooms in the compact machine
 * dimension.
 */
public class CompactMachineConnectionGraph {

    private final MutableValueGraph<IGraphNode, IGraphEdge> graph;
    private final Map<Integer, CompactMachineNode> machines;
    private final Map<ChunkPos, CompactMachineRoomNode> rooms;

    public static final Codec<CompactMachineConnectionGraph> CODEC = RecordCodecBuilder.create(i -> i.group(
            CompactMachineConnectionInfo.CODEC.listOf()
                    .fieldOf("connections")
                    .forGetter(CompactMachineConnectionGraph::buildConnections)
    ).apply(i, CompactMachineConnectionGraph::new));



    public CompactMachineConnectionGraph() {
        graph = ValueGraphBuilder
                .directed()
                .build();

        machines = new HashMap<>();
        rooms = new HashMap<>();
    }

    private CompactMachineConnectionGraph(List<CompactMachineConnectionInfo> connections) {
        this();

        for(CompactMachineConnectionInfo i : connections) {
            addRoom(i.roomChunk);
            for(int connectedMachine : i.machines()) {
                addMachine(connectedMachine);
                connectMachineToRoom(connectedMachine, i.roomChunk);
            }
        }
    }

    private List<CompactMachineConnectionInfo> buildConnections() {
        List<CompactMachineConnectionInfo> result = new ArrayList<>();
        this.rooms.forEach((chunk, node) -> {
            try {
                Collection<Integer> machines = this.getMachinesFor(chunk);
                CompactMachineConnectionInfo roomInfo = new CompactMachineConnectionInfo(chunk, machines);
                result.add(roomInfo);
            } catch (NonexistentRoomException e) {
                CompactMachines.LOGGER.error(e);
            }
        });

        return result;
    }

    public void addMachine(int machine) {
        if(this.machines.containsKey(machine))
            return;

        CompactMachineNode node = new CompactMachineNode(machine);
        graph.addNode(node);
        machines.put(machine, node);
    }

    public void addRoom(ChunkPos roomChunk) {
        if(this.rooms.containsKey(roomChunk))
            return;

        CompactMachineRoomNode node = new CompactMachineRoomNode(roomChunk);
        graph.addNode(node);
        rooms.put(roomChunk, node);
    }

    public void connectMachineToRoom(int machine, ChunkPos room) {
        if(!machines.containsKey(machine))
            return;

        if(!rooms.containsKey(room))
            return;

        CompactMachineNode machineNode = machines.get(machine);
        CompactMachineRoomNode roomNode = rooms.get(room);

        graph.putEdgeValue(machineNode, roomNode, DefaultEdges.machineToRoom());
    }

    public Collection<Integer> getMachinesFor(ChunkPos machineChunk) throws NonexistentRoomException {
        var node = this.rooms.get(machineChunk);
        if(node == null)
            throw new NonexistentRoomException(machineChunk);

        var inbound = graph.predecessors(node);
        return inbound.stream()
                .filter(ibn -> ibn instanceof CompactMachineNode)
                .map(ibn -> (CompactMachineNode) ibn)
                .map(CompactMachineNode::machineId)
                .collect(Collectors.toSet());
    }

    public Optional<ChunkPos> getConnectedRoom(int machine) {
        if(!this.machines.containsKey(machine))
            return Optional.empty();

        var node = this.machines.get(machine);
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
        if(!machines.containsKey(machine))
            return;

        final var node = machines.get(machine);
        graph.removeNode(node);
        machines.remove(machine);
    }

    public void removeRoom(ChunkPos room) {
        if(!this.rooms.containsKey(room))
            return;

        graph.removeNode(rooms.get(room));
        rooms.remove(room);
    }

    /**
     * Data structure for serialization. Do not use directly.
     */
    private static class CompactMachineConnectionInfo {
        private final ChunkPos roomChunk;
        private final List<Integer> connectedMachines;

        public static final Codec<CompactMachineConnectionInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
                CodecExtensions.CHUNKPOS
                        .fieldOf("machine")
                        .forGetter(CompactMachineConnectionInfo::room),

                Codec.INT.listOf()
                        .fieldOf("connections")
                        .forGetter(CompactMachineConnectionInfo::machines)
        ).apply(i, CompactMachineConnectionInfo::new));

        public CompactMachineConnectionInfo(ChunkPos roomChunk, Collection<Integer> connections) {
            this.roomChunk = roomChunk;
            this.connectedMachines = ImmutableList.copyOf(connections);
        }

        public ChunkPos room() {
            return this.roomChunk;
        }

        public List<Integer> machines() {
            return this.connectedMachines;
        }
    }
}
