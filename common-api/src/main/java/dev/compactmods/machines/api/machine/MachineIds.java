package dev.compactmods.machines.api.machine;

import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public interface MachineIds {
    ResourceLocation BLOCK_ENTITY = new ResourceLocation(Constants.MOD_ID, "compact_machine");

    ResourceLocation UNBOUND_MACHINE_ITEM_ID = new ResourceLocation(Constants.MOD_ID, "new_machine");
    ResourceKey<Item> UNBOUND_MACHINE_ITEM_KEY = ResourceKey.create(Registry.ITEM_REGISTRY, UNBOUND_MACHINE_ITEM_ID);
}
