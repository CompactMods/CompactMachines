package dev.compactmods.machines.api.tunnels.recipe;

import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public abstract class TunnelRecipeHelper {
    public static ResourceLocation getRecipeId(@Nonnull ResourceLocation tunnelType) {
        return new ResourceLocation(tunnelType.getNamespace(), "tunnels/" + tunnelType.getPath());
    }
}