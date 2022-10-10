package dev.compactmods.machines.api.shrinking;

import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public interface PSDTags {
    TagKey<Item> ITEM = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Constants.MOD_ID, "shrinking_device"));
}
