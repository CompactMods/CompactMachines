package dev.compactmods.machines.api.tunnels.recipe;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public abstract class TunnelRecipeHelper {
    public static ResourceLocation getRecipeId(@NotNull ResourceLocation tunnelType) {
        return new ResourceLocation(tunnelType.getNamespace(), "dev/compactmods/machines/api/tunnels/" + tunnelType.getPath());
    }
}