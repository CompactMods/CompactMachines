package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class BoundCompactMachineItem extends BlockItem {

    public static final String FALLBACK_ID = Util.makeDescriptionId("block", CompactMachinesCore.identifier("bound_machine_fallback"));

    public BoundCompactMachineItem(Properties builder) {
        super(Machines.Blocks.MACHINE.get(), builder);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        var roomCode = stack.get(Rooms.DataComponents.BOUND_ROOM_CODE);
        if (roomCode != null) {
            // TODO - Server-synced room name list
            // tooltip.add(TranslationUtil.tooltip(Tooltips.ROOM_NAME, room));
            tooltipAdder.accept(Component.translatableWithFallback(MachineTranslations.IDs.BOUND_TO, "Bound To: %s", roomCode));
        }
    }
}

