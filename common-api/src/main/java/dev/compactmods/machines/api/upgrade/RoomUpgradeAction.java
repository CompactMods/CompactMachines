package dev.compactmods.machines.api.upgrade;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public interface RoomUpgradeAction {

    ResourceKey<Registry<RoomUpgradeAction>> REG_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "upgrade_actions"));

    Codec<? extends RoomUpgradeAction> codec();
}
