package dev.compactmods.machines.data.graph;

public class DefaultEdges {

    public static IGraphEdge machineToRoom(){
        return new MachineLinkEdge();
    }
}
