package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a machine item that has not been bound to a room yet,
 * but has an assigned template to use.
 */
public class UnboundCompactMachineItem extends BlockItem {

    public UnboundCompactMachineItem(Properties builder) {
        super(Machines.Blocks.UNBOUND_MACHINE.get(), builder);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatableWithFallback(Util.makeDescriptionId("machine", getTemplateId(stack)), "Compact Machine");
    }

    @Override
    public ItemStack getDefaultInstance() {
        var stack = new ItemStack(this);
        stack.set(CMDataComponents.ROOM_TEMPLATE_ID, RoomTemplate.NO_TEMPLATE);
        stack.set(CMDataComponents.MACHINE_COLOR, MachineColors.WHITE);
        return stack;
    }

    private ResourceLocation getTemplateId(ItemStack stack) {
        return stack.get(CMDataComponents.ROOM_TEMPLATE_ID);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag);

        tooltip.accept(Component.translatableWithFallback(MachineTranslations.IDs.NEW_MACHINE, "New Machine"));

        if(stack.has(CMDataComponents.ROOM_TEMPLATE_ID)) {
            // TODO Room Dimensions
            Component.literal(stack.get(CMDataComponents.ROOM_TEMPLATE_ID).toString());
        }
    }
}
