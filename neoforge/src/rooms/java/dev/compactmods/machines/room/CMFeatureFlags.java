package dev.compactmods.machines.room;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

public class CMFeatureFlags {

    public static final FeatureFlag ROOM_UPGRADES_FLAG = FeatureFlags.REGISTRY.getFlag(CompactMachinesCore.identifier("room_upgrades"));

    public static final FeatureFlagSet ROOM_UPGRADES = FeatureFlagSet.of(ROOM_UPGRADES_FLAG);
}
