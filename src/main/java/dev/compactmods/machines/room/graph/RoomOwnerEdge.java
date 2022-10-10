package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.room.Rooms;
import org.jetbrains.annotations.NotNull;

public record RoomOwnerEdge() implements IGraphEdge {
    public static final Codec<RoomOwnerEdge> CODEC = Codec.unit(new RoomOwnerEdge());

    @Override
    public @NotNull IGraphEdgeType<RoomOwnerEdge> getEdgeType() {
        return Rooms.ROOM_OWNER_EDGE.get();
    }
}
