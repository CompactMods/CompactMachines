package dev.compactmods.machines.upgrades.storage;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Registry of the {@link ResourceStorageType}s known to the mod, and the built-in item and fluid
 * storage kinds backed by NeoForge's {@code Capabilities.Item.BLOCK} / {@code Capabilities.Fluid.BLOCK}.
 */
public final class ResourceStorageTypes {

    public static final DeferredRegister<ResourceStorageType<?>> REGISTRY =
            DeferredRegister.create(ResourceStorageType.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    public static final DeferredHolder<ResourceStorageType<?>, ResourceStorageType<ItemResource>> ITEM_BLOCK =
            REGISTRY.register("item_block", () -> new ResourceStorageType<>(
                    Capabilities.Item.BLOCK, ItemResource.CODEC, ItemResource.STREAM_CODEC));

    public static final DeferredHolder<ResourceStorageType<?>, ResourceStorageType<FluidResource>> FLUID_BLOCK =
            REGISTRY.register("fluid_block", () -> new ResourceStorageType<>(
                    Capabilities.Fluid.BLOCK, FluidResource.CODEC, FluidResource.STREAM_CODEC));

    private ResourceStorageTypes() {}

    public static void init(IEventBus modBus) {
        REGISTRY.makeRegistry(builder -> builder.sync(true));
        REGISTRY.register(modBus);
    }
}
