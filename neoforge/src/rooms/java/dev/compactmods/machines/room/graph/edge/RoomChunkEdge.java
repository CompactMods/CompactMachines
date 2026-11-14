package dev.compactmods.machines.room.graph.edge;

import dev.compactmods.feather.edge.GraphEdge;
import dev.compactmods.machines.room.graph.node.RoomChunkNode;
import dev.compactmods.machines.room.graph.node.RoomReferenceNode;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.WeakReference;

public record RoomChunkEdge(WeakReference<RoomReferenceNode> source, WeakReference<RoomChunkNode> target)
        implements GraphEdge<RoomReferenceNode, RoomChunkNode> {

    public RoomChunkEdge(RoomReferenceNode source, RoomChunkNode target) {
        this(new WeakReference<>(source), new WeakReference<>(target));
    }
}
