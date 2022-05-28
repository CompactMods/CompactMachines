package dev.compactmods.machines.graph;

import org.jetbrains.annotations.NotNull;

public interface IGraphEdge {
    @NotNull IGraphEdgeType getEdgeType();
}
