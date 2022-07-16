package dev.compactmods.machines.api.upgrade;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class RoomUpgradeHelper {

    public static final String NBT_UPGRADE_NODE = "upgrade_info";
    public static final String NBT_UPDATE_ID = "key";

    public static Optional<ResourceLocation> getTypeFrom(@NotNull ItemStack stack) {
        if(!stack.hasTag()) return Optional.empty();
        final var tag = stack.getTag();
        if(!tag.contains(NBT_UPGRADE_NODE)) return Optional.empty();

        final var upg = tag.getCompound(NBT_UPGRADE_NODE);
        if(!upg.contains(NBT_UPDATE_ID)) return Optional.empty();

        final var upg2 = new ResourceLocation(upg.getString(NBT_UPDATE_ID));
        return Optional.of(upg2);
    }
}
