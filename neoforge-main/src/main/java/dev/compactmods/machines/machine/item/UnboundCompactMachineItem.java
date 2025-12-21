package dev.compactmods.machines.machine.item;

import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a machine item that has not been bound to a room yet,
 * but has an assigned template to use.
 */
public class UnboundCompactMachineItem extends BlockItem {

    public UnboundCompactMachineItem(Properties builder) {
        super(Machines.Blocks.UNBOUND_MACHINE.get(), builder);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return Component.translatableWithFallback(getDescriptionId(pStack), "Compact Machine");
    }

    @NotNull
    @Override
    public String getDescriptionId(ItemStack stack) {
        return Util.makeDescriptionId("machine", getTemplateId(stack));
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        super.appendHoverText(stack, context, tooltip, flags);

        tooltip.add(Component.translatableWithFallback(MachineTranslations.IDs.NEW_MACHINE, "New Machine")
                .withColor(CommonColors.LIGHT_GRAY));

        if(stack.has(CMDataComponents.ROOM_TEMPLATE_ID)) {
            var templateID = stack.get(CMDataComponents.ROOM_TEMPLATE_ID);
            var template = RoomTemplateHelper.getTemplate(Objects.requireNonNull(context.registries()), templateID);
            if(template != RoomTemplate.INVALID_TEMPLATE) {
                if(flags.hasShiftDown())
                    tooltip.add(Component.translatable(MachineTranslations.IDs.SIZE,
                        template.internalDimensions().toString())
                            .withColor(FastColor.ARGB32.color(120, 120, 120)));

                if(flags.hasShiftDown())
                    tooltip.add(Component.literal(templateID.toString())
                            .withColor(FastColor.ARGB32.color(50, 50, 50)));
            }
        }
    }
}
