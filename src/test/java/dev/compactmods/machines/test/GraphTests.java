package dev.compactmods.machines.test;

import java.util.Collection;
import java.util.Optional;
import com.google.common.graph.Graph;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.data.codec.CodecExtensions;
import dev.compactmods.machines.data.graph.CompactMachineConnectionGraph;
import dev.compactmods.machines.data.graph.CompactMachineNode;
import dev.compactmods.machines.data.graph.CompactMachineRoomNode;
import dev.compactmods.machines.data.graph.IMachineGraphNode;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Machine Graph Tests")
@org.junit.jupiter.api.Tag("minecraft")
public class GraphTests {

    private CompactMachineConnectionGraph generateGraphWithSingleRoom() {
        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        g.addMachine(0);
        g.addRoom(new ChunkPos(0, 0));

        g.connectMachineToRoom(0, new ChunkPos(0, 0));
        return g;
    }

    private CompactMachineConnectionGraph generateGraphWithMultipleRooms(int numRooms) {
        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        for (int i = 0; i < numRooms; i++) {
            g.addMachine(i);
            ChunkPos chunk = MathUtil.getChunkForRoomIndex(i);
            g.addRoom(chunk);
            g.connectMachineToRoom(i, chunk);
        }

        return g;
    }

    private void verifySingleRoomValid(CompactMachineConnectionGraph graph, int machine, ChunkPos room) {
        Optional<ChunkPos> connectedRoom = graph.getConnectedRoom(machine);
        Assertions.assertTrue(connectedRoom.isPresent());

        connectedRoom.ifPresent(cRoom -> {
            Assertions.assertEquals(room, cRoom);
        });
    }

    @Test
    @DisplayName("Can Create Basic Graph")
    void basicGraph() {

        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        Assertions.assertEquals(0, g.getMachines().count());

        // At construction, no machines or rooms are registered
        // The method itself should just return an empty collection in this scenario
        Assertions.assertDoesNotThrow(() -> g.getMachinesFor(new ChunkPos(0, 0)));

        // Make sure that there's no linked machines here
        Collection<Integer> linkedMachines = g.getMachinesFor(new ChunkPos(0, 0));
        Assertions.assertNotNull(linkedMachines);
        Assertions.assertEquals(0, linkedMachines.size());

        // Make sure there's no linked rooms
        Optional<ChunkPos> connectedRoom = g.getConnectedRoom(0);
        Assertions.assertNotNull(connectedRoom);
        Assertions.assertFalse(connectedRoom.isPresent());
    }

    @Test
    @DisplayName("Create Single Linked Machine (1:1)")
    void canCreateGraphWithLinkedMachine() {
        int machine = 0;
        ChunkPos room = new ChunkPos(0, 0);

        CompactMachineConnectionGraph g = generateGraphWithSingleRoom();

        verifySingleRoomValid(g, machine, room);

        Collection<Integer> linkedMachines = g.getMachinesFor(room);
        Assertions.assertEquals(1, linkedMachines.size());
        Assertions.assertTrue(linkedMachines.contains(machine));
    }


    @Test
    @DisplayName("Create Multiple Rooms (1:1)")
    void canCreateMultipleRoomsWithSingleLinkedMachine() {
        CompactMachineConnectionGraph graph = generateGraphWithMultipleRooms(10);

        for (int roomIndex = 0; roomIndex < 10; roomIndex++) {
            final ChunkPos EXPECTED_CHUNK = MathUtil.getChunkForRoomIndex(roomIndex);

            verifySingleRoomValid(graph, roomIndex, EXPECTED_CHUNK);
        }
    }

    @Test
    @DisplayName("Create Multiple Linked Machines (M:1)")
    void canCreateRoomWithMultipleLinkedMachines() {
        int MACHINE_1 = 0;
        int MACHINE_2 = 1;
        ChunkPos EXPECTED_ROOM = new ChunkPos(0, 0);

        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        g.addMachine(0);
        g.addMachine(1);

        ChunkPos roomChunk = new ChunkPos(0, 0);
        g.addRoom(roomChunk);
        g.connectMachineToRoom(0, roomChunk);
        g.connectMachineToRoom(1, roomChunk);

        verifySingleRoomValid(g, 0, EXPECTED_ROOM);
        verifySingleRoomValid(g, 1, EXPECTED_ROOM);

        Collection<Integer> linkedMachines = g.getMachinesFor(EXPECTED_ROOM);
        Assertions.assertEquals(2, linkedMachines.size());
        Assertions.assertTrue(linkedMachines.contains(MACHINE_1));
        Assertions.assertTrue(linkedMachines.contains(MACHINE_2));
    }

    @Test
    @DisplayName("Correctly serializes to NBT")
    void canSerialize() {
        CompactMachineConnectionGraph graph = generateGraphWithSingleRoom();

        DataResult<Tag> nbtResult = CompactMachineConnectionGraph.CODEC.encodeStart(NbtOps.INSTANCE, graph);

        nbtResult.resultOrPartial(Assertions::fail)
                .ifPresent(nbt -> {
                    Assertions.assertTrue(nbt instanceof CompoundTag);

                    CompoundTag c = (CompoundTag) nbt;
                    Assertions.assertFalse(c.isEmpty());

                    Assertions.assertTrue(c.contains("connections"));

                    ListTag connections = c.getList("connections", Tag.TAG_COMPOUND);
                    Assertions.assertEquals(1, connections.size(), "Expected one connection from a machine to a single room.");

                    CompoundTag conn1 = connections.getCompound(0);
                    Assertions.assertNotNull(conn1);

                    Assertions.assertTrue(conn1.contains("machine"));
                    Assertions.assertTrue(conn1.contains("connections"));

                    Tag machineChunk = conn1.get("machine");
                    DataResult<ChunkPos> chunkRes = CodecExtensions.CHUNKPOS.parse(NbtOps.INSTANCE, machineChunk);
                    chunkRes.resultOrPartial(Assertions::fail)
                            .ifPresent(chunk -> {
                                Assertions.assertEquals(new ChunkPos(0, 0), chunk);
                            });

                    ListTag connList = conn1.getList("connections", Tag.TAG_COMPOUND);
                    Assertions.assertNotNull(connList);
                    Assertions.assertEquals(1, connList.size());
                    Assertions.assertEquals(0, connList.getInt(0));
                });
    }

    @Test
    void simpleNestedMachines() {
        /*
            Overworld - Contains Machine 0, linked to Room 0
            CompactWorld - Contains Machine 1, linked to Room 1 (Inside Room 0)
         */

        CompactMachineConnectionGraph graph = new CompactMachineConnectionGraph();

        graph.addMachine(0);
        graph.addMachine(1);

        // Add two rooms
        ChunkPos room0 = MathUtil.getChunkForRoomIndex(0);
        ChunkPos room1 = MathUtil.getChunkForRoomIndex(1);
        graph.addRoom(room0);
        graph.addRoom(room1);

        graph.connectMachineToRoom(0, room0);
        graph.connectMachineToRoom(1, room1);

    }

    public static String write(final CompactMachineConnectionGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("strict digraph G {")
                .append(System.lineSeparator())
                .append("\tlayout = fdp;").append(System.lineSeparator())
                .append("\tnode [shape=square,style=filled,color=lightgray];").append(System.lineSeparator());

        sb.append("}");
        return sb.toString();
    }

    private static void outputMachineInside(Graph<IMachineGraphNode> graph, StringBuilder sb, IMachineGraphNode inside) {
        if (inside instanceof CompactMachineRoomNode) {
            CompactMachineRoomNode min = (CompactMachineRoomNode) inside;

            String insideClusterName = "cluster_" + min.getId();
            graph.predecessors(min).forEach(incoming -> {
                sb.append("\t")
                        .append(incoming.getId())
                        .append("->")
                        .append(insideClusterName)
                        .append(System.lineSeparator());
            });

            sb.append("\t")
                    .append("subgraph ").append(insideClusterName)
                    .append(" {")
                    .append(System.lineSeparator());

            sb.append("\t\t")
                    .append(String.format("graph [label=\"%s\",style=filled,color=cadetblue]", min.label()))
                    .append(System.lineSeparator());

            graph.successors(min).forEach(insideNode -> {
                if (insideNode instanceof CompactMachineNode) {
                    CompactMachineNode men = (CompactMachineNode) insideNode;
                    outputExternalNode(sb, men);
                }
            });

            sb.append("\t")
                    .append("}").append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
    }

    private static void outputExternalNode(StringBuilder sb, CompactMachineNode men) {
        boolean connected = true;
        sb.append("\t")
                .append(men.getId())
                .append(String.format(" [label=\"%s\",style=filled,color=%s]", men.label(), connected ? "lightgray" : "palevioletred1"))
                .append(System.lineSeparator());
    }
}
