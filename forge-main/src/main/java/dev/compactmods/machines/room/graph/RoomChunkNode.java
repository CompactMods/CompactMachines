package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.IRoomLookup;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.world.level.ChunkPos;

public record RoomChunkNode(ChunkPos chunk) implements IGraphNode<RoomChunkNode> {

    public static final Codec<RoomChunkNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.CHUNKPOS.fieldOf("chunk").forGetter(RoomChunkNode::chunk)
    ).apply(i, RoomChunkNode::new));

    @Override
    public IGraphNodeType<RoomChunkNode> getType() {
        return Rooms.ROOM_CHUNK_NODE.get();
    }

    public IRoomRegistration room(IRoomLookup lookup) {
        return lookup.findByChunk(chunk).orElseThrow();
    }
}
