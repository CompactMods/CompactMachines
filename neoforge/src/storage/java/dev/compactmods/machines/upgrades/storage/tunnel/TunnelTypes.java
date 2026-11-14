package dev.compactmods.machines.upgrades.storage.tunnel;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.resources.ResourceKey;

/** Keys of the built-in {@link TunnelType}s shipped in the mod's data. */
public interface TunnelTypes {
    ResourceKey<TunnelType> ITEM = ResourceKey.create(TunnelType.REGISTRY_KEY, CompactMachinesCore.identifier("item"));
    ResourceKey<TunnelType> FLUID = ResourceKey.create(TunnelType.REGISTRY_KEY, CompactMachinesCore.identifier("fluid"));
}
