package dev.compactmods.machines.graph;

import org.jetbrains.annotations.NotNull;

public interface IGraphEdge<T extends IGraphEdge<T>> {
    @NotNull IGraphEdgeType<T> getEdgeType();
}
