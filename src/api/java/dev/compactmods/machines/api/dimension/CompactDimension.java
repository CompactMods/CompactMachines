package dev.compactmods.machines.api.dimension;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public abstract class CompactDimension {
    public static final ResourceKey<Level> LEVEL_KEY = ResourceKey
            .create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    public static final ResourceKey<DimensionType> DIM_TYPE_KEY = ResourceKey
            .create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    private CompactDimension() {}
}
