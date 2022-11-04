package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.machine.MachineNbt;
import net.minecraft.world.item.ItemStack;

public interface ICompactMachineItem {

    static ItemStack setColor(ItemStack stack, int color) {
        var tag = stack.getOrCreateTag();
        tag.putInt(MachineNbt.NBT_COLOR, color);
        return stack;
    }

    static int getMachineColor(ItemStack stack) {
        if (!stack.hasTag()) return 0xFFFFFFFF;

        final var tag = stack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(MachineNbt.NBT_COLOR))
            return 0xFFFFFFFF;

        return tag.getInt(MachineNbt.NBT_COLOR);
    }

}
