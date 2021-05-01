package com.robotgryphon.compactmachines.data.graph;

import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactmachines.data.codec.CodecExtensions;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores information on how external machines connect to the rooms in the compact machine
 * dimension.
 */
public class CompactMachineConnectionGraph {

    private final MutableGraph<IMachineGraphNode> graph;
    private final Map<Integer, CompactMachineNode> machines;
    private final Map<ChunkPos, CompactMachineRoomNode> rooms;

    private static final Codec<CompactMachineConnectionInfo> CONNECTION_INFO_CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.CHUNKPOS_CODEC
                    .fieldOf("machine")
                    .forGetter(CompactMachineConnectionInfo::room),

            Codec.INT.listOf()
                    .fieldOf("connections")
                    .forGetter(CompactMachineConnectionInfo::machines)
    ).apply(i, CompactMachineConnectionInfo::new));

    public static final Codec<CompactMachineConnectionGraph> CODEC = RecordCodecBuilder.create(i -> i.group(
            CONNECTION_INFO_CODEC.listOf()
                    .fieldOf("connections")
                    .forGetter(CompactMachineConnectionGraph::buildConnections)
    ).apply(i, CompactMachineConnectionGraph::new));



    public CompactMachineConnectionGraph() {
        graph = GraphBuilder
                .directed()
                .nodeOrder(ElementOrder.sorted(Comparator.comparing(IMachineGraphNode::getId)))
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
        return Collections.emptyList();
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

        graph.putEdge(machineNode, roomNode);
    }

    public Collection<Integer> getMachinesFor(ChunkPos machineChunk) {
        CompactMachineRoomNode node = this.rooms.get(machineChunk);
        if(node == null)
            return Collections.emptySet();

        Set<IMachineGraphNode> inbound = graph.predecessors(node);
        return inbound.stream()
                .filter(ibn -> ibn instanceof CompactMachineNode)
                .map(ibn -> (CompactMachineNode) ibn)
                .map(CompactMachineNode::getMachineId)
                .collect(Collectors.toSet());
    }

    public Optional<ChunkPos> getConnectedRoom(int machine) {
        if(!this.machines.containsKey(machine))
            return Optional.empty();

        CompactMachineNode node = this.machines.get(machine);
        Set<IMachineGraphNode> connected = this.graph.successors(node);
        return connected.stream()
                .filter(n -> n instanceof CompactMachineRoomNode)
                .map(n -> (CompactMachineRoomNode) n)
                .map(CompactMachineRoomNode::getChunk)
                .findFirst();
    }

    public Stream<CompactMachineNode> getMachines() {
        return this.machines.values().stream();
    }

    /**
     * Data structure for serialization. Do not use directly.
     */
    private static class CompactMachineConnectionInfo {
        private final ChunkPos roomChunk;
        private final List<Integer> connectedMachines;

        public CompactMachineConnectionInfo(ChunkPos roomChunk, List<Integer> connections) {
            this.roomChunk = roomChunk;
            this.connectedMachines = connections;
        }

        public ChunkPos room() {
            return this.roomChunk;
        }

        public List<Integer> machines() {
            return this.connectedMachines;
        }
    }
}
