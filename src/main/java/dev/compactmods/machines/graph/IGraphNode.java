package dev.compactmods.machines.graph;

public interface IGraphNode<T extends IGraphNode<T>> {
    IGraphNodeType<T> getType();
}
