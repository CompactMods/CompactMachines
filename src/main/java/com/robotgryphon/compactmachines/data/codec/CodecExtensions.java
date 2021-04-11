package com.robotgryphon.compactmachines.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.UUID;

public abstract class CodecExtensions {

    public static Codec<UUID> UUID_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("uuid").forGetter(UUID::toString)
    ).apply(i, UUID::fromString));

    public static Codec<Vector3d> VECTOR3D_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.DOUBLE.fieldOf("x").forGetter(Vector3d::x),
            Codec.DOUBLE.fieldOf("y").forGetter(Vector3d::y),
            Codec.DOUBLE.fieldOf("z").forGetter(Vector3d::z)
    ).apply(i, Vector3d::new));

    public static Codec<ChunkPos> CHUNKPOS_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("chunkX").forGetter((c) -> c.x),
            Codec.INT.fieldOf("chunkZ").forGetter((c) -> c.z)
    ).apply(i, ChunkPos::new));
}
