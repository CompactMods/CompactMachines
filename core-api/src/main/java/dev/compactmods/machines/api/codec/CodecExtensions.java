package dev.compactmods.machines.api.codec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class CodecExtensions {

    public static final Codec<Vec2> VEC2 = Codec.FLOAT.listOf()
            .comapFlatMap((vec) -> Util.fixedSize(vec, 2).map(
                            (res) -> new Vec2(res.get(0), res.get(1))),
                    (vec) -> List.of(vec.x, vec.y));

    public static final Codec<ChunkPos> CHUNKPOS = Codec.INT_STREAM
            .comapFlatMap(i -> Util.fixedSize(i, 2)
                    .map(arr -> new ChunkPos(arr[0], arr[1])), pos -> IntStream.of(pos.x, pos.z));

    public static <T extends Enum<T> & StringRepresentable> StreamCodec<ByteBuf, T> stringRepresentableStreamCodec(T[] values) {
        final var lookup = StringRepresentable.createNameLookup(values, StringRepresentable::getSerializedName);
        return ByteBufCodecs.STRING_UTF8.map(lookup, T::getSerializedName);
    }
}
