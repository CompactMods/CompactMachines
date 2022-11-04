package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.api.core.CMTags;
import dev.compactmods.machines.api.machine.IMachineBlockEntity;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

public class MachineColors {

    public static final ItemColor ITEM = (stack, pTintIndex) -> {
        if(!stack.is(CMTags.MACHINE_ITEM)) return 0xFFFFFFFF;
        return pTintIndex == 0 ? ICompactMachineItem.getMachineColor(stack) : 0xFFFFFFFF;
    };

    public static final BlockColor BLOCK = (state, level, pos, tintIndex) -> {
        if(!state.is(CMTags.MACHINE_BLOCK) || level == null || pos == null)
            return 0xFFFFFFFF;

        if(!(level.getBlockEntity(pos) instanceof IMachineBlockEntity machineData))
            return 0xFFFFFFFF;

        return tintIndex == 0 ? machineData.getColor() : 0xFFFFFFFF;
    };
}
