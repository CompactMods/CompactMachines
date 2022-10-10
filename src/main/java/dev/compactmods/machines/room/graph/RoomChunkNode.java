package dev.compactmods.machines.room.graph;

import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.IRoomLookup;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.world.level.ChunkPos;

public record RoomChunkNode(ChunkPos chunk) implements IGraphNode<RoomChunkNode> {

    @Override
    public IGraphNodeType<RoomChunkNode> getType() {
        return null;
    }

    public IRoomRegistration room(IRoomLookup lookup) {
        return lookup.findByChunk(chunk).orElseThrow();
    }
}
