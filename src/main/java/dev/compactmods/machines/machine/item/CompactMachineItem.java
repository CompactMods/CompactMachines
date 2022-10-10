package dev.compactmods.machines.machine.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CompactMachineItem extends BlockItem {
    public static final String NBT_COLOR = "machine_color";

    public CompactMachineItem(Block blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public static ItemStack setColor(ItemStack stack, int color) {
        var tag = stack.getOrCreateTag();
        tag.putInt(NBT_COLOR, color);
        return stack;
    }

    public static int getMachineColor(ItemStack stack) {
        if (!stack.hasTag()) return 0xFFFFFFFF;

        final var tag = stack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(NBT_COLOR))
            return 0xFFFFFFFF;

        return tag.getInt(NBT_COLOR);
    }
}
