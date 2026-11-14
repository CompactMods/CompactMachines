package dev.compactmods.machines.api.machine.block;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface IBoundCompactMachineBlockEntity extends ICompactMachineBlockEntity {
    String NBT_OWNER = "owner";

    boolean setCore(@Nullable Player player, ItemStack newCore);

    ItemStack popCore(@Nullable Player player);
}
