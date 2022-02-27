package dev.compactmods.machines.graph;

import dev.compactmods.machines.machine.graph.MachineLinkEdge;

public class DefaultEdges {

    public static IGraphEdge machineToRoom(){
        return new MachineLinkEdge();
    }

}
