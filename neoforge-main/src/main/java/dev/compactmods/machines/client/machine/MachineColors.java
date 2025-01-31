package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;

public class MachineColors {

    private static final int DEFAULT = 0xFFFFFFFF;

    public static final ItemColor ITEM = (stack, pTintIndex) -> {
        if (!stack.is(MachineConstants.MACHINE_ITEM)) return DEFAULT;
        return pTintIndex == 0 ? stack.getOrDefault(CMDataComponents.MACHINE_COLOR, dev.compactmods.machines.machine.MachineColors.WHITE).rgb()
                : DEFAULT;
    };

    public static final BlockColor BLOCK = (state, level, pos, tintIndex) -> {
        if (!state.is(MachineConstants.MACHINE_BLOCK) || level == null || pos == null)
            return DEFAULT;

        var be = level.getBlockEntity(pos);
        if (be != null)
            return tintIndex == 0 ? be.getData(CMDataAttachments.MACHINE_COLOR).rgb() : DEFAULT;

        return DEFAULT;
    };
}
