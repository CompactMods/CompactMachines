package dev.compactmods.machines.api.shrinking;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface PSDTags {
    /**
     * Marks an item as a personal shrinking device.
     */
    @Deprecated(forRemoval = true)
    TagKey<Item> ITEM = TagKey.create(Registries.ITEM, CompactMachines.modRL("shrinking_device"));
}
