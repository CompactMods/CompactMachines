package dev.compactmods.machines.forge.room.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.codec.CodecExtensions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record RoomPreview(ChunkPos chunk, BoundingBox area) {
    public static final Codec<RoomPreview> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.CHUNKPOS.fieldOf("pos").forGetter(RoomPreview::chunk),
            BoundingBox.CODEC.fieldOf("area").forGetter(RoomPreview::area)
    ).apply(i, RoomPreview::new));
}
