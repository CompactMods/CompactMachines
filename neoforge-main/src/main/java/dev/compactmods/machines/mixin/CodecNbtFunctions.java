package dev.compactmods.machines.mixin;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;
import java.util.Optional;

public interface CodecNbtFunctions {

    private CompoundTag self() {
        return (CompoundTag) (Object) this;
    }

    default <T> void store(String key, Codec<T> codec, T data) {
        this.store(key, codec, NbtOps.INSTANCE, data);
    }

    default <T> void storeNullable(String key, Codec<T> codec, @Nullable T data) {
        if (data != null) {
            this.store(key, codec, data);
        }
    }

    default <T> void store(String key, Codec<T> codec, DynamicOps<Tag> ops, T data) {
        self().put(key, codec.encodeStart(ops, data).getOrThrow());
    }

    default <T> void storeNullable(String key, Codec<T> codec, DynamicOps<Tag> ops, @Nullable T data) {
        if (data != null) {
            this.store(key, codec, ops, data);
        }
    }

    default <T> void store(MapCodec<T> codec, T data) {
        this.store(codec, NbtOps.INSTANCE, data);
    }

    default <T> void store(MapCodec<T> codec, DynamicOps<Tag> ops, T data) {
        self().merge((CompoundTag)codec.encoder().encodeStart(ops, data).getOrThrow());
    }

    default <T> Optional<T> read(String key, Codec<T> codec) {
        return this.read(key, codec, NbtOps.INSTANCE);
    }

    default <T> Optional<T> read(String key, Codec<T> codec, DynamicOps<Tag> ops) {
        Tag tag = self().get(key);
        return tag == null ? Optional.empty() : codec.parse(ops, tag).resultOrPartial($$2x -> {
            var LOGGER = LogUtils.getLogger();
            LOGGER.error("Failed to read field ({}={}): {}", key, tag, $$2x);
        });
    }

    default <T> Optional<T> read(MapCodec<T> codec) {
        return this.read(codec, NbtOps.INSTANCE);
    }

    default <T> Optional<T> read(MapCodec<T> codec, DynamicOps<Tag> ops) {
        return codec.decode(ops, ops.getMap(self()).getOrThrow()).resultOrPartial(datax -> {
            var LOGGER = LogUtils.getLogger();
            LOGGER.error("Failed to read value ({}): {}", this, datax);
        });
    }
}

