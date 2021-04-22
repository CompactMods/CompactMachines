package com.robotgryphon.compactmachines.tests;

import com.google.common.graph.*;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.data.graph.CompactMachineInsideNode;
import com.robotgryphon.compactmachines.data.graph.CompactMachineNode;
import com.robotgryphon.compactmachines.data.graph.IMachineGraphNode;
import com.robotgryphon.compactmachines.data.graph.MachineExternalLocation;
import com.robotgryphon.compactmachines.util.MathUtil;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class GraphTests {

    @Test
    void canCreateBasicGraph() {
        MutableGraph<IMachineGraphNode> g = GraphBuilder
                .directed()
                .nodeOrder(ElementOrder.sorted(Comparator.comparing(IMachineGraphNode::getId)))
                .build();

        HashMap<String, IMachineGraphNode> lookup = new HashMap<>();

        generateData(g, lookup);

        String w = write(g);

        try (PrintWriter pw = new PrintWriter("graph.dot")) {
            pw.println(w);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(true);
    }

    private void generateData(MutableGraph<IMachineGraphNode> g, HashMap<String, IMachineGraphNode> lookup) {
        Random r = new Random();
        MachineExternalLocation[] values = MachineExternalLocation.values();
        int numInsides = 0;
        int numOutsides = 0;

        Set<IMachineGraphNode> disconnected = new HashSet<>();
        List<CompactMachineNode> externals = new ArrayList<>();
        List<CompactMachineInsideNode> internals = new ArrayList<>();

        // Seed a couple of machines and insides so they're always there
        for(int i = 0; i < 10; i++) {
            ChunkPos machineChunk = getMachineChunkPos(numInsides + 1);
            CompactMachineNode extern = createMachineExternalNode(g, lookup, i);
            CompactMachineInsideNode intern = createMachineInternalNode(g, lookup, machineChunk);

            externals.add(extern);
            internals.add(intern);

            extern.connectTo(intern);
            numOutsides++;
            numInsides++;
        }

        for(int i = 0; i < 50; i++) {

            if(r.nextBoolean()) {
                // Creating the outside of a machine
                MachineExternalLocation loc = values[r.nextInt(values.length)];

                CompactMachineNode machine = createMachineExternalNode(g, lookup, numOutsides + 1);
                externals.add(machine);

                switch (loc) {
                    case EXTERNAL_DIMENSION:
                        int randomMachineInsideE = r.nextInt(internals.size());
                        CompactMachineInsideNode miE = internals.get(randomMachineInsideE);
                        machine.connectTo(miE);

                        // try to remove from disconnected if it exists there
                        disconnected.remove(miE);
                        break;

                    case INSIDE_MACHINE:
                        int randomMachineInsideI = r.nextInt(internals.size());
                        CompactMachineInsideNode miI = internals.get(randomMachineInsideI);

                        // Put the machine inside a randomly chosen existing machine
                        g.putEdge(miI, machine);

                        boolean connectToAnother = r.nextBoolean();
                        if(connectToAnother) {
                            System.out.println("connect");
                            int randomMachine = r.nextInt(internals.size());
                            CompactMachineInsideNode in = internals.get(randomMachine);
                            machine.connectTo(in);
                        }
                        break;
                }

                numOutsides++;
            } else {
                // Creating the inside of a machine
                ChunkPos machineChunk= getMachineChunkPos(numInsides + 1);
                CompactMachineInsideNode mi = createMachineInternalNode(g, lookup, machineChunk);
                disconnected.add(mi);
                numInsides++;
            }
        }

        if(!disconnected.isEmpty()) {
            for (IMachineGraphNode di : disconnected) {
                CompactMachineNode machine = createMachineExternalNode(g, lookup, numOutsides + 1);
                machine.connectTo(di);
                numOutsides++;
            }
        }

        disconnected.clear();
    }

    private ChunkPos getMachineChunkPos(int i) {
        Vector3i pos = MathUtil.getRegionPositionByIndex(i);
        ChunkPos machineChunk = new ChunkPos(pos.getX(), pos.getZ());
        return machineChunk;
    }

    private CompactMachineInsideNode createMachineInternalNode(MutableGraph<IMachineGraphNode> g, HashMap<String, IMachineGraphNode> lookup, ChunkPos id) {
        CompactMachineInsideNode intern = new CompactMachineInsideNode(id);
        g.addNode(intern);
        lookup.put(intern.getId(), intern);
        return intern;
    }

    private CompactMachineNode createMachineExternalNode(MutableGraph<IMachineGraphNode> g, HashMap<String, IMachineGraphNode> lookup, int id) {
        CompactMachineNode extern = new CompactMachineNode(g, id);
        g.addNode(extern);
        lookup.put(extern.getId(), extern);
        return extern;
    }

    public static String write(final Graph<IMachineGraphNode> graph) {
        StringBuilder sb = new StringBuilder();
        sb
                .append("strict digraph G {")
                .append(System.lineSeparator())
                .append("\tlayout = fdp;").append(System.lineSeparator())
                .append("\tnode [shape=square,style=filled,color=lightgray];").append(System.lineSeparator());

        Set<CompactMachineNode> topLevelMachines = graph.nodes().stream()
                .filter(n -> n instanceof CompactMachineNode)
                .map(n -> (CompactMachineNode) n)
                .filter(n -> graph.inDegree(n) == 0)
                .collect(Collectors.toSet());

        for (CompactMachineNode n : topLevelMachines)
            outputExternalNode(sb, n);

        graph.nodes().stream()
                .filter(n -> n instanceof CompactMachineInsideNode)
                .map(n -> (CompactMachineInsideNode) n)
                .forEach(n -> {
                    outputMachineInside(graph, sb, n);
                });

        sb.append("}");
        return sb.toString();
    }

    private static void outputMachineInside(Graph<IMachineGraphNode> graph, StringBuilder sb, IMachineGraphNode inside) {
        if (inside instanceof CompactMachineInsideNode) {
            CompactMachineInsideNode min = (CompactMachineInsideNode) inside;

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
                if(insideNode instanceof CompactMachineNode) {
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
        boolean connected = men.isConnected;
        sb.append("\t")
                .append(men.getId())
                .append(String.format(" [label=\"%s\",style=filled,color=%s]", men.label(), men.isConnected ? "lightgray" : "palevioletred1"))
                .append(System.lineSeparator());
    }
}
