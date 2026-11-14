package dev.compactmods.machines.api.room.generation;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.NonNull;

public record RoomDimensions(int width, int depth, int height) {
    private static final Codec<RoomDimensions> FULL_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.validate(RoomDimensions::validateDimension).fieldOf("width").forGetter(RoomDimensions::width),
            Codec.INT.validate(RoomDimensions::validateDimension).fieldOf("depth").forGetter(RoomDimensions::depth),
            Codec.INT.validate(RoomDimensions::validateDimension).fieldOf("height").forGetter(RoomDimensions::height)
    ).apply(inst, RoomDimensions::new));

    private static DataResult<Integer> validateDimension(Integer integer) {
        if(integer < 3)
            return DataResult.error(() -> "Dimensions must be at least 3 blocks in width on any given side.");

        if(integer > 255)
            return DataResult.error(() -> "Dimensions must not exceed 255 blocks in width on any given side. Also, why on earth do you need a room THAT large?!?");

        return DataResult.success(integer);
    }

    public static final Codec<RoomDimensions> CODEC = Codec.of(FULL_CODEC, Decoder.INSTANCE);

    public static final StreamCodec<ByteBuf, RoomDimensions> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public static RoomDimensions cubic(int cubic) {
        int clamped = Math.clamp(cubic, 3, 255);
        return new RoomDimensions(clamped, clamped, clamped);
    }

    @Override
    public @NonNull String toString() {
        return "%s x %s x %s".formatted(width, depth, height);
    }

    public int maxDimension() {
        return Math.max(Math.max(width, depth), height);
    }

    private static class Decoder implements com.mojang.serialization.Decoder<RoomDimensions> {

        public static final Decoder INSTANCE = new Decoder();

        @Override
        public <T> DataResult<Pair<RoomDimensions, T>> decode(DynamicOps<T> dynamicOps, T t) {
            final var asNum = dynamicOps.withParser(Codec.intRange(3, 45)).apply(t);
            if (asNum.isSuccess())
                return asNum.result()
                        .map(singleSize -> DataResult.success(Pair.of(RoomDimensions.cubic(singleSize), t))).orElseThrow();

            final var asObj = dynamicOps.withParser(FULL_CODEC).apply(t);
            if (asObj.isSuccess())
                return asObj.result()
                        .map(dims -> DataResult.success(Pair.of(dims, t))).orElseThrow();

            return DataResult.error(() -> "Dimensions must either be a single integer between 3-45, or specify width/depth/height.");
        }
    }
}
