package com.robotgryphon.compactmachines.tests;

import com.google.common.graph.Graph;
import com.robotgryphon.compactmachines.data.graph.CompactMachineConnectionGraph;
import com.robotgryphon.compactmachines.data.graph.CompactMachineNode;
import com.robotgryphon.compactmachines.data.graph.CompactMachineRoomNode;
import com.robotgryphon.compactmachines.data.graph.IMachineGraphNode;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

@DisplayName("Machine Graph Tests")
public class GraphTests {

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

        CompactMachineConnectionGraph g = new CompactMachineConnectionGraph();

        g.addMachine(0);
        g.addRoom(new ChunkPos(0, 0));

        g.connectMachineToRoom(0, new ChunkPos(0, 0));

        Optional<ChunkPos> connectedRoom = g.getConnectedRoom(machine);
        Assertions.assertTrue(connectedRoom.isPresent());

        connectedRoom.ifPresent(cRoom -> {
            Assertions.assertEquals(room, cRoom);
        });

        Collection<Integer> linkedMachines = g.getMachinesFor(room);
        Assertions.assertEquals(1, linkedMachines.size());
        Assertions.assertTrue(linkedMachines.contains(machine));
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

        Optional<ChunkPos> connectedRoom = g.getConnectedRoom(0);
        Assertions.assertTrue(connectedRoom.isPresent());
        connectedRoom.ifPresent(cRoom -> {
            Assertions.assertEquals(EXPECTED_ROOM, cRoom);
        });

        Optional<ChunkPos> connectedRoom2 = g.getConnectedRoom(1);
        Assertions.assertTrue(connectedRoom2.isPresent());
        connectedRoom2.ifPresent(cRoom2 -> {
            Assertions.assertEquals(EXPECTED_ROOM, cRoom2);
        });

        Collection<Integer> linkedMachines = g.getMachinesFor(EXPECTED_ROOM);
        Assertions.assertEquals(2, linkedMachines.size());
        Assertions.assertTrue(linkedMachines.contains(MACHINE_1));
        Assertions.assertTrue(linkedMachines.contains(MACHINE_2));
    }

//    private void generateData(MutableGraph<IGraphNode> g, HashMap<String, IGraphNode> lookup) {
//        Random r = new Random();
//        MachineExternalLocation[] values = MachineExternalLocation.values();
//        int numInsides = 0;
//        int numOutsides = 0;
//
//        Set<IMachineGraphNode> disconnected = new HashSet<>();
//        List<CompactMachineExternalNode> externals = new ArrayList<>();
//        List<CompactMachineInsideNode> internals = new ArrayList<>();
//
//        // Seed a couple of machines and insides so they're always there
//        for(int i = 0; i < 10; i++) {
//            ChunkPos machineChunk = getMachineChunkPos(numInsides + 1);
//            CompactMachineExternalNode extern = createMachineExternalNode(g, lookup, i);
//            CompactMachineInsideNode intern = createMachineInternalNode(g, lookup, machineChunk);
//
//            externals.add(extern);
//            internals.add(intern);
//
//            extern.connectTo(intern);
//            numOutsides++;
//            numInsides++;
//        }
//
//        for(int i = 0; i < 50; i++) {
//
//            if(r.nextBoolean()) {
//                // Creating the outside of a machine
//                MachineExternalLocation loc = values[r.nextInt(values.length)];
//
//                CompactMachineExternalNode machine = createMachineExternalNode(g, lookup, numOutsides + 1);
//                externals.add(machine);
//
//                switch (loc) {
//                    case EXTERNAL_DIMENSION:
//                        int randomMachineInsideE = r.nextInt(internals.size());
//                        CompactMachineInsideNode miE = internals.get(randomMachineInsideE);
//                        machine.connectTo(miE);
//
//                        // try to remove from disconnected if it exists there
//                        disconnected.remove(miE);
//                        break;
//
//                    case INSIDE_MACHINE:
//                        int randomMachineInsideI = r.nextInt(internals.size());
//                        CompactMachineInsideNode miI = internals.get(randomMachineInsideI);
//
//                        // Put the machine inside a randomly chosen existing machine
//                        g.putEdge(miI, machine);
//
//                        boolean connectToAnother = r.nextBoolean();
//                        if(connectToAnother) {
//                            System.out.println("connect");
//                            int randomMachine = r.nextInt(internals.size());
//                            CompactMachineInsideNode in = internals.get(randomMachine);
//                            machine.connectTo(in);
//
//                            g.successors(machine); // All the connections internally
//                        }
//                        break;
//                }
//
//                numOutsides++;
//            } else {
//                // Creating the inside of a machine
//                ChunkPos machineChunk= getMachineChunkPos(numInsides + 1);
//                CompactMachineInsideNode mi = createMachineInternalNode(g, lookup, machineChunk);
//                disconnected.add(mi);
//                numInsides++;
//            }
//        }
//
//        if(!disconnected.isEmpty()) {
//            for (IMachineGraphNode di : disconnected) {
//                CompactMachineExternalNode machine = createMachineExternalNode(g, lookup, numOutsides + 1);
//                machine.connectTo(di);
//                numOutsides++;
//            }
//        }
//
//        disconnected.clear();
//    }

    public static String write(final CompactMachineConnectionGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("strict digraph G {")
                .append(System.lineSeparator())
                .append("\tlayout = fdp;").append(System.lineSeparator())
                .append("\tnode [shape=square,style=filled,color=lightgray];").append(System.lineSeparator());

//        Set<CompactMachineNode> topLevelMachines = graph.getMachines()
//                .filter(n -> graph.getConnectedRoom(n.getMachineId()).isPresent())
//                .collect(Collectors.toSet());
//
//        for (CompactMachineNode n : topLevelMachines)
//            outputExternalNode(sb, n);
//
//        graph.nodes().stream()
//                .filter(n -> n instanceof CompactMachineRoomNode)
//                .map(n -> (CompactMachineRoomNode) n)
//                .forEach(n -> {
//                    outputMachineInside(graph, sb, n);
//                });

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
