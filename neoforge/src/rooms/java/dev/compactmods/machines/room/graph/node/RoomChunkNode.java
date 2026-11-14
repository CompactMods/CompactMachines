package dev.compactmods.machines.room.graph.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.node.Node;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public record RoomChunkNode(UUID id, Data data) implements Node<RoomChunkNode.Data> {

    public record Data(ChunkPos chunk) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(i -> i.group(
                ChunkPos.CODEC.fieldOf("chunk").forGetter(Data::chunk)
        ).apply(i, Data::new));
    }
}
