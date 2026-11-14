package dev.compactmods.machines.machine.i18n;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

public interface MachineTranslations {

    Function<BlockPos, Component> NOT_A_MACHINE_BLOCK = (pos) -> Component.empty();

    interface IDs {
        String OWNER = Util.makeDescriptionId("machine", CompactMachinesCore.identifier("machine.owner"));
        String SIZE = Util.makeDescriptionId("machine", CompactMachinesCore.identifier("machine.size"));
        String BOUND_TO = Util.makeDescriptionId("machine", CompactMachinesCore.identifier("machine.bound_to"));
        String NEW_MACHINE = Util.makeDescriptionId("machine", CompactMachinesCore.identifier("new_machine"));
    }
}
