package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.room.Rooms;
import org.jetbrains.annotations.NotNull;

public record RoomChunkEdge() implements dev.compactmods.machines.graph.IGraphEdge {

    public static final Codec<RoomChunkEdge> CODEC = Codec.unit(new RoomChunkEdge());

    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return Rooms.ROOM_CHUNK_EDGE.get();
    }
}
