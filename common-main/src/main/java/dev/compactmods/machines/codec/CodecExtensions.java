package dev.compactmods.machines.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.ICompactMachinesMod;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public abstract class CodecExtensions {
    public static final Codec<Vec3> VECTOR3D = DoubleStreamExtensions.CODEC
            .comapFlatMap(i -> DoubleStreamExtensions.fixedDoubleSize(i, 3)
                    .map(out -> new Vec3(out[0], out[1], out[2])), vec -> DoubleStream.of(vec.x, vec.y, vec.z));

    public static final Codec<Vec2> VEC2 = Codec.FLOAT.listOf()
            .comapFlatMap((vec) -> Util.fixedSize(vec, 2).map(
                            (res) -> new Vec2(res.get(0), res.get(1))),
                    (vec) -> List.of(vec.x, vec.y));

    public static final Codec<ChunkPos> CHUNKPOS = Codec.INT_STREAM
            .comapFlatMap(i -> Util.fixedSize(i, 2)
                    .map(arr -> new ChunkPos(arr[0], arr[1])), pos -> IntStream.of(pos.x, pos.z));

    @Deprecated(forRemoval = true, since = "5.2.0")
    public static final Codec<UUID> UUID_STRING = Codec.STRING.comapFlatMap((s) -> {
        try {
            return DataResult.success(UUID.fromString(s));
        } catch (Exception ex) {
            return DataResult.error("Not a valid UUID: " + s + " (" + ex.getMessage() + ")");
        }
    }, UUID::toString).stable();

    public static <T> CompoundTag writeIntoTag(Codec<T> codec, T instance, CompoundTag tag) {

        final var encoded = codec
                .encodeStart(NbtOps.INSTANCE, instance)
                .getOrThrow(false, ICompactMachinesMod.LOGGER::fatal);

        if (encoded instanceof CompoundTag ect)
            tag.merge(ect);

        return tag;
    }
}
