package dev.compactmods.machines.upgrades.storage.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.ResourceStorageType;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.Optional;

/**
 * A kind of tunnel that can be put on a room wall, loaded from datapacks
 * ({@code data/<namespace>/compactmachines/tunnel_types/}) and synced to clients.
 *
 * <p>Links a {@link ResourceTypes resource type} to the {@link ResourceStorageType} whose block
 * capability the tunnel exposes, both by key, and carries what sits between them: how the tunnel is
 * shown, and what buys a named storage its capacity.
 *
 * @param resourceType         The {@link Resource} class this tunnel moves, by its key in {@link ResourceTypes}.
 * @param storageType          The storage type supplying the block capability the tunnel wall exposes.
 * @param color                Tint for the tunnel, packed {@code 0xRRGGBB}.
 * @param icon                 GUI sprite id for the tunnel's icon.
 * @param container            Item that buys a named storage capacity; each one adds {@code capacityPerContainer}.
 * @param capacityPerContainer Capacity added per container: slots for items, buckets for fluids.
 */
public record TunnelType(
        ResourceKey<Class<? extends Resource>> resourceType,
        ResourceKey<ResourceStorageType<?>> storageType,
        int color,
        Identifier icon,
        Holder<Item> container,
        int capacityPerContainer
) {

    public static final ResourceKey<Registry<TunnelType>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(CompactMachinesCore.identifier("tunnel_types"));

    public static final Codec<TunnelType> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceKey.codec(ResourceTypes.REGISTRY_KEY).fieldOf("resource_type").forGetter(TunnelType::resourceType),
            ResourceKey.codec(ResourceStorageType.REGISTRY_KEY).fieldOf("storage_type").forGetter(TunnelType::storageType),
            ExtraCodecs.STRING_RGB_COLOR.fieldOf("color").forGetter(TunnelType::color),
            Identifier.CODEC.fieldOf("icon").forGetter(TunnelType::icon),
            BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("container").forGetter(TunnelType::container),
            ExtraCodecs.POSITIVE_INT.fieldOf("capacity_per_container").forGetter(TunnelType::capacityPerContainer)
    ).apply(inst, TunnelType::new));

    /** {@return the resource class this tunnel moves}, or empty if its key isn't registered */
    public Optional<Class<? extends Resource>> resolveResourceType() {
        return ResourceTypes.get(resourceType);
    }

    /** {@return the storage type this tunnel exposes}, or empty if its key isn't registered */
    @SuppressWarnings("unchecked")
    public Optional<ResourceStorageType<?>> resolveStorageType() {
        return BuiltInRegistries.REGISTRY
                .getOptional(ResourceStorageType.REGISTRY_KEY.identifier())
                .map(r -> (Registry<ResourceStorageType<?>>) r)
                .flatMap(r -> r.getOptional(storageType));
    }

    /** {@return the capacity bought by {@code containers} containers} */
    public int capacity(int containers) {
        return containers * capacityPerContainer;
    }

    /** {@return the translation key for this tunnel type's display name} */
    public static String translationKey(Identifier id) {
        return id.toLanguageKey("tunnel_type");
    }
}
