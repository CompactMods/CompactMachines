package dev.compactmods.machines.test;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import com.google.common.graph.Graph;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.data.codec.CodecExtensions;
import dev.compactmods.machines.data.graph.CompactMachineConnectionGraph;
import dev.compactmods.machines.data.graph.CompactMachineNode;
import dev.compactmods.machines.data.graph.CompactMachineRoomNode;
import dev.compactmods.machines.data.graph.IMachineGraphNode;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactMachines.MOD_ID)
public class GraphTests {

    private static CompactMachineConnectionGraph generateGraphWithSingleRoom() {
        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        g.addMachine(0);
        g.addRoom(new ChunkPos(0, 0));

        g.connectMachineToRoom(0, new ChunkPos(0, 0));
        return g;
    }

    private static CompactMachineConnectionGraph generateGraphWithMultipleRooms(int numRooms) {
        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        for (int i = 0; i < numRooms; i++) {
            g.addMachine(i);
            ChunkPos chunk = MathUtil.getChunkForRoomIndex(i);
            g.addRoom(chunk);
            g.connectMachineToRoom(i, chunk);
        }

        return g;
    }

    private static void verifySingleRoomValid(GameTestHelper test, CompactMachineConnectionGraph graph, int machine, ChunkPos room) {
        Optional<ChunkPos> connectedRoom = graph.getConnectedRoom(machine);
        if (connectedRoom.isEmpty())
            test.fail("Connected room not found.");

        connectedRoom.ifPresent(cRoom -> {
            if (!room.equals(cRoom))
                test.fail("Room not equal.");
        });
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void basicGraph(final GameTestHelper test) {

        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        if (g.getMachines().findAny().isPresent())
            test.fail("Graph should have been empty after construction.");

        // At construction, no machines or rooms are registered
        // The method itself should just return an empty collection in this scenario
        try {
            g.getMachinesFor(new ChunkPos(0, 0));
        } catch (Exception e) {
            test.fail(e.getMessage());
        }

        // Make sure that there's no linked machines here
        Collection<Integer> linkedMachines = g.getMachinesFor(new ChunkPos(0, 0));
        if (linkedMachines == null)
            test.fail("getMachinesFor should return an empty collection, not null");

        if (!linkedMachines.isEmpty())
            test.fail("Linked machine collection should be empty.");

        // Make sure there's no linked rooms
        Optional<ChunkPos> connectedRoom = g.getConnectedRoom(0);
        Objects.requireNonNull(connectedRoom);
        if (connectedRoom.isPresent())
            test.fail("No room connections should be present.");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void canCreateGraphWithLinkedMachine(final GameTestHelper test) {
        int machine = 0;
        ChunkPos room = new ChunkPos(0, 0);

        CompactMachineConnectionGraph g = generateGraphWithSingleRoom();

        verifySingleRoomValid(test, g, machine, room);

        Collection<Integer> linkedMachines = g.getMachinesFor(room);
        if (1 != linkedMachines.size())
            test.fail("Expected exactly one linked machine; got " + linkedMachines.size());

        if (!linkedMachines.contains(machine))
            test.fail("Expected machine 0 to be linked; did not exist in linked machine collection");

        test.succeed();
    }


    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void canCreateMultipleRoomsWithSingleLinkedMachine(final GameTestHelper test) {
        CompactMachineConnectionGraph graph = generateGraphWithMultipleRooms(10);

        for (int roomIndex = 0; roomIndex < 10; roomIndex++) {
            final ChunkPos EXPECTED_CHUNK = MathUtil.getChunkForRoomIndex(roomIndex);

            verifySingleRoomValid(test, graph, roomIndex, EXPECTED_CHUNK);
        }

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void canCreateRoomWithMultipleLinkedMachines(final GameTestHelper test) {
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

        verifySingleRoomValid(test, g, 0, EXPECTED_ROOM);
        verifySingleRoomValid(test, g, 1, EXPECTED_ROOM);

        Collection<Integer> linkedMachines = g.getMachinesFor(EXPECTED_ROOM);
        if (2 != linkedMachines.size())
            test.fail("Linked machine count was not correct");

        if (!linkedMachines.contains(MACHINE_1))
            test.fail("1st machine not found in linked machine set");
        ;
        if (!linkedMachines.contains(MACHINE_2))
            test.fail("2nd machine not found in linked machine set");

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void canSerialize(final GameTestHelper test) {
        CompactMachineConnectionGraph graph = generateGraphWithSingleRoom();

        DataResult<Tag> nbtResult = CompactMachineConnectionGraph.CODEC.encodeStart(NbtOps.INSTANCE, graph);

        nbtResult.resultOrPartial(test::fail)
                .ifPresent(nbt -> {
                    if (!(nbt instanceof CompoundTag graphData)) {
                        test.fail("Encoded graph not expected tag type");
                        return;
                    }

                    if(graphData.isEmpty())
                        test.fail("Encoded tag does not have any data.");

                    if(!graphData.contains("connections"))
                        test.fail("Connection info not found.");

                    ListTag connections = graphData.getList("connections", Tag.TAG_COMPOUND);
                    if(1 != connections.size())
                        test.fail("Expected one connection from a machine to a single room.");

                    CompoundTag room1 = connections.getCompound(0);

                    if(!room1.contains("machine"))
                        test.fail("Machine info in connection not found.");

                    if(!room1.contains("connections"))
                        test.fail("Machine connection info not found.");

                    Tag machineChunk = room1.get("machine");
                    DataResult<ChunkPos> chunkRes = CodecExtensions.CHUNKPOS.parse(NbtOps.INSTANCE, machineChunk);
                    chunkRes.resultOrPartial(test::fail)
                            .ifPresent(chunk -> {
                                if(!new ChunkPos(0, 0).equals(chunk))
                                    test.fail("Room chunk location is not correct.");
                            });

                    ListTag roomMachineConnections = room1.getList("connections", Tag.TAG_INT);
                    if(1 != roomMachineConnections.size())
                        test.fail("Expected exactly 1 machine to be connected to the room.");

                    if(0 != roomMachineConnections.getInt(0))
                        test.fail("Expected the connected machine ID to be 0.");
                });

        test.succeed();
    }

    @GameTest(template = "empty_1x1", batch = TestBatches.MACHINE_GRAPH)
    public static void simpleNestedMachines(final GameTestHelper test) {
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

        test.succeed();
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
