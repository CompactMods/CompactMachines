package dev.compactmods.machines.upgrades.storage;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

/**
 * A registered kind of resource storage, e.g. item or fluid.
 *
 * <p>Ties a concrete resource type {@code T} to the {@link BlockCapability} used to resolve its
 * handler from a block, and to the codecs used to (de)serialize instances of that resource. This
 * is the runtime witness that {@link NamedResourceStorage} carries so that resolution and
 * serialization work despite generic erasure: the registered type's id is what gets persisted,
 * and the type it resolves back to supplies the capability and resource codecs.
 *
 * @param blockCapability      Block capability that yields a {@code ResourceHandler<T>}
 *                             (e.g. {@code Capabilities.Item.BLOCK}).
 * @param resourceCodec        Codec for the resource type {@code T}.
 * @param resourceStreamCodec  Network codec for the resource type {@code T}.
 * @param <T>                  The resource type held by handlers of this storage kind.
 */
public record ResourceStorageType<T extends Resource>(
        BlockCapability<ResourceHandler<T>, @Nullable Direction> blockCapability,
        Codec<T> resourceCodec,
        StreamCodec<RegistryFriendlyByteBuf, T> resourceStreamCodec
) {

    public static final ResourceKey<Registry<ResourceStorageType<?>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(CompactMachinesCore.identifier("resource_storage_type"));

    /**
     * Codec that (de)serializes a storage type by its registry id. Lazy because the registry is not
     * populated until mod init has run.
     */
    @SuppressWarnings("unchecked")
    public static final Codec<ResourceStorageType<?>> CODEC = Codec.lazyInitialized(() ->
            BuiltInRegistries.REGISTRY
                    .getOptional(CompactMachinesCore.identifier("resource_storage_type"))
                    .map(r -> (Registry<ResourceStorageType<?>>) r)
                    .map(Registry::byNameCodec)
                    .orElseThrow(() -> new IllegalStateException(
                            "Resource storage type registry not registered yet; calling too early?")));

    /** Network codec that reads/writes a storage type by its id from the synced registry. */
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStorageType<?>> STREAM_CODEC =
            ByteBufCodecs.registry(REGISTRY_KEY);
}
