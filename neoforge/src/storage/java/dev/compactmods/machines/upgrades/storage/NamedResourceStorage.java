package dev.compactmods.machines.upgrades.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

/**
 * A named, serializable handle to a block's resource handler.
 *
 * <p>Pairs a human-friendly, user-assignable name with a {@link ResourceStoragePosition}, so the
 * storage can be queried and referred to by name in upgrades and in the room management user
 * interfaces. The handler itself is not stored; it is resolved on demand via
 * {@link #resolve(MinecraftServer)}.
 *
 * @param name    A human-friendly, user-assignable name for this storage.
 * @param storage Where the handler lives and how to resolve it.
 * @param <T>     The resource type held by the handler (e.g. {@code ItemResource}).
 */
public record NamedResourceStorage<T extends Resource>(String name, ResourceStoragePosition<T> storage) {

    /**
     * Codec for a handle. The location keys are inlined flatly ({@code name}, {@code position},
     * {@code direction}, {@code type}). The decoded handle is wildcard-typed
     * ({@code NamedResourceStorage<?>}) because the resource type is only known dynamically from the
     * registry.
     */
    public static final Codec<NamedResourceStorage<?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("name").forGetter(NamedResourceStorage::name),
            ResourceStoragePosition.MAP_CODEC.forGetter(NamedResourceStorage::storage)
    ).apply(inst, NamedResourceStorage::create));

    /** Network codec matching {@link #CODEC}. */
    public static final StreamCodec<RegistryFriendlyByteBuf, NamedResourceStorage<?>> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, NamedResourceStorage::name,
            ResourceStoragePosition.STREAM_CODEC, NamedResourceStorage::storage,
            NamedResourceStorage::create);

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static NamedResourceStorage<?> create(String name, ResourceStoragePosition<?> storage) {
        return new NamedResourceStorage(name, storage);
    }

    public GlobalPos position() {
        return storage.position();
    }

    public @Nullable Direction direction() {
        return storage.direction();
    }

    public ResourceStorageType<T> type() {
        return storage.type();
    }

    /**
     * Resolves the live resource handler for this storage. Delegates to
     * {@link ResourceStoragePosition#resolve(MinecraftServer)}.
     */
    public @Nullable ResourceHandler<T> resolve(MinecraftServer server) {
        return storage.resolve(server);
    }
}
