package dev.compactmods.machines.room.graph;

import dev.compactmods.machines.graph.IGraphEdgeType;
import org.jetbrains.annotations.NotNull;

public record RoomChunkEdge() implements dev.compactmods.machines.graph.IGraphEdge {
    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return null;
    }
}
