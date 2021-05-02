package com.robotgryphon.compactmachines.data.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.system.CallbackI;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public abstract class CodecExtensions {
    public static Codec<UUID> UUID_CODEC = Codec.STRING
            .comapFlatMap((s) -> {
                try {
                    return DataResult.success(UUID.fromString(s));
                } catch (Exception ex) {
                    return DataResult.error("Not a valid UUID: " + s + " (" + ex.getMessage() + ")");
                }
            }, UUID::toString).stable();

    public static Codec<Vector3d> VECTOR3D_CODEC = DoubleStreamExtensions.CODEC
            .comapFlatMap(i -> DoubleStreamExtensions.fixedDoubleSize(i, 3)
                    .map(out -> new Vector3d(out[0], out[1], out[2])), vec -> DoubleStream.of(vec.x, vec.y, vec.z));

    public static Codec<ChunkPos> CHUNKPOS_CODEC = Codec.INT_STREAM
            .comapFlatMap(i -> Util.fixedSize(i, 2)
                    .map(arr -> new ChunkPos(arr[0], arr[1])), pos -> IntStream.of(pos.x, pos.z));
}
