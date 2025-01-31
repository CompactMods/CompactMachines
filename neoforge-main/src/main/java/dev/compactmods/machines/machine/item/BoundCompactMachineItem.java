package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoundCompactMachineItem extends BlockItem {

    public static final String NBT_ROOM_DIMENSIONS = "room_dimensions";

    private static final String FALLBACK_ID = Util.makeDescriptionId("block", CompactMachines.modRL("bound_machine_fallback"));

    public BoundCompactMachineItem(Properties builder) {
        super(Machines.Blocks.BOUND_MACHINE.get(), builder);
    }

    @Override
    public Component getName(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_NAME, Component.translatableWithFallback(FALLBACK_ID, "Compact Machine"));
    }

    @NotNull
    @Override
    public String getDescriptionId(ItemStack stack) {
        return FALLBACK_ID;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltips, TooltipFlag flags) {
        super.appendHoverText(stack, ctx, tooltips, flags);

        var roomCode = stack.get(CMDataComponents.BOUND_ROOM_CODE);
        if (roomCode != null) {
            // TODO - Server-synced room name list
            // tooltip.add(TranslationUtil.tooltip(Tooltips.ROOM_NAME, room));
            tooltips.add(Component.translatableWithFallback(MachineTranslations.IDs.BOUND_TO, "Bound To: %s", roomCode));
        }
    }
}

