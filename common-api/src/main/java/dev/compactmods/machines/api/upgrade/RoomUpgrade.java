package dev.compactmods.machines.api.upgrade;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.core.CMRegistryKeys;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface RoomUpgrade {

    ResourceKey<Registry<RoomUpgrade>> REG_KEY = CMRegistryKeys.UPGRADES;

    Codec<? extends RoomUpgrade> codec();
}
