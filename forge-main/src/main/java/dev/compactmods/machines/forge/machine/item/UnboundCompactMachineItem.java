package dev.compactmods.machines.forge.machine.item;

import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.machine.MachineEntityNbt;
import dev.compactmods.machines.api.machine.MachineIds;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a machine item that has not been bound to a room yet,
 * but has an assigned template to use.
 */
public class UnboundCompactMachineItem extends BlockItem implements ICompactMachineItem {

    public UnboundCompactMachineItem(Properties builder) {
        super(Machines.UNBOUND_MACHINE_BLOCK.get(), builder);
    }

    @NotNull
    @Override
    public String getDescriptionId(ItemStack stack) {
        return Util.makeDescriptionId("machine", MachineItemUtil.getTemplateId(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        // We need NBT data for the rest of this
        boolean sneaking = Screen.hasShiftDown();

        if (sneaking) {
            MachineItemUtil.getTemplate(stack).ifPresent(actualTemplate -> {
                final var roomDimensions = actualTemplate.dimensions();
                tooltip.add(Component.literal("Size: " + roomDimensions.toShortString()).withStyle(ChatFormatting.YELLOW));

                final var templateId = MachineItemUtil.getTemplateId(stack);
                tooltip.add(Component.literal("Template: " + templateId).withStyle(ChatFormatting.DARK_GRAY));

                if (!actualTemplate.prefillTemplate().equals(RoomTemplate.NO_TEMPLATE)) {
                    tooltip.add(Component.literal("Prefill: " + actualTemplate.prefillTemplate()).withStyle(ChatFormatting.DARK_GRAY));
                }
            });
        } else {
            MutableComponent text = TranslationUtil.tooltip(Tooltips.HINT_HOLD_SHIFT)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .withStyle(ChatFormatting.ITALIC);

            tooltip.add(text);
        }
    }

    private static ItemLike fromRegistry() {
        return ForgeRegistries.ITEMS.getValue(MachineIds.UNBOUND_MACHINE_ITEM_ID);
    }

    public static ItemStack unbound() {
        final var stack = new ItemStack(fromRegistry(), 1);
        MachineItemUtil.setTemplate(stack, RoomTemplate.NO_TEMPLATE);
        ICompactMachineItem.setColor(stack, 0xFFFFFFFF);
        return stack;
    }

    public static ItemStack forTemplate(ResourceLocation templateId, RoomTemplate template) {
        final var stack = new ItemStack(fromRegistry(), 1);
        MachineItemUtil.setTemplate(stack, templateId);
        ICompactMachineItem.setColor(stack, template.color());

        final var tag = stack.getOrCreateTag();
        tag.putString(MachineEntityNbt.NBT_TEMPLATE_ID, templateId.toString());
        return stack;
    }
}
