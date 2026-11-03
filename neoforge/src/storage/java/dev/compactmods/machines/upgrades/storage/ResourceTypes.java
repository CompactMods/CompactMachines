package dev.compactmods.machines.upgrades.storage;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.Optional;

/**
 * Registry of the NeoForge {@link Resource} types known to the mod, registered directly by class.
 *
 * <p>Other mods add their own resource types by registering their {@code Resource} class under
 * {@link #REGISTRY_KEY} with their own {@code DeferredRegister}. Tunnel types refer to entries here
 * by key (see {@link dev.compactmods.machines.upgrades.storage.tunnel.TunnelType#resourceType()}).
 */
public final class ResourceTypes {

    public static final ResourceKey<Registry<Class<? extends Resource>>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(CompactMachinesCore.identifier("resource_types"));

    public static final DeferredRegister<Class<? extends Resource>> REGISTRY =
            DeferredRegister.create(REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    public static final DeferredHolder<Class<? extends Resource>, Class<ItemResource>> ITEM =
            REGISTRY.register("item", () -> ItemResource.class);

    public static final DeferredHolder<Class<? extends Resource>, Class<FluidResource>> FLUID =
            REGISTRY.register("fluid", () -> FluidResource.class);

    private ResourceTypes() {}

    public static void init(IEventBus modBus) {
        REGISTRY.makeRegistry(builder -> builder.sync(true));
        REGISTRY.register(modBus);
    }

    /** {@return the registered resource class for {@code key}}, or empty if nothing is registered under it */
    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends Resource>> get(ResourceKey<Class<? extends Resource>> key) {
        return BuiltInRegistries.REGISTRY
                .getOptional(REGISTRY_KEY.identifier())
                .map(r -> (Registry<Class<? extends Resource>>) r)
                .flatMap(r -> r.getOptional(key));
    }
}
