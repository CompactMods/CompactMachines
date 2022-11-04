package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.graph.SimpleGraphEdgeType;
import org.jetbrains.annotations.NotNull;

public record RoomChunkEdge() implements dev.compactmods.machines.graph.IGraphEdge {

    public static final Codec<RoomChunkEdge> CODEC = Codec.unit(new RoomChunkEdge());

    public static final IGraphEdgeType<RoomChunkEdge> EDGE_TYPE = SimpleGraphEdgeType.instance(CODEC);

    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return EDGE_TYPE;
    }
}
