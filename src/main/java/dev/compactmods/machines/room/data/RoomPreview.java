package dev.compactmods.machines.room.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.world.level.ChunkPos;

public record RoomPreview(ChunkPos chunk, RoomSize size) {
    public static final Codec<RoomPreview> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.CHUNKPOS.fieldOf("pos").forGetter(RoomPreview::chunk),
            RoomSize.CODEC.fieldOf("size").forGetter(RoomPreview::size)
    ).apply(i, RoomPreview::new));
}
