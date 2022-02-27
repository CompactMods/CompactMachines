package dev.compactmods.machines.codec;

import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public abstract class CodecExtensions {
    public static final Codec<UUID> UUID_CODEC = Codec.STRING
            .comapFlatMap((s) -> {
                try {
                    return DataResult.success(UUID.fromString(s));
                } catch (Exception ex) {
                    return DataResult.error("Not a valid UUID: " + s + " (" + ex.getMessage() + ")");
                }
            }, UUID::toString).stable();

    public static final Codec<Vec3> VECTOR3D = DoubleStreamExtensions.CODEC
            .comapFlatMap(i -> DoubleStreamExtensions.fixedDoubleSize(i, 3)
                    .map(out -> new Vec3(out[0], out[1], out[2])), vec -> DoubleStream.of(vec.x, vec.y, vec.z));

    public static final Codec<ChunkPos> CHUNKPOS = Codec.INT_STREAM
            .comapFlatMap(i -> Util.fixedSize(i, 2)
                    .map(arr -> new ChunkPos(arr[0], arr[1])), pos -> IntStream.of(pos.x, pos.z));
}
