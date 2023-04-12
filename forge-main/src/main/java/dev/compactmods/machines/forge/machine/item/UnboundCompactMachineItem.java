package dev.compactmods.machines.forge.machine.item;

import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.machine.MachineIds;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.data.MachineDataTagBuilder;
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
import java.util.Optional;

/**
 * Represents a machine item that has not been bound to a room yet,
 * but has an assigned template to use.
 */
public class UnboundCompactMachineItem extends BlockItem implements ICompactMachineItem {

    public static final String NBT_TEMPLATE_ID = MachineNbt.NBT_TEMPLATE_ID;

    public UnboundCompactMachineItem(Properties builder) {
        super(Machines.MACHINE_BLOCK.get(), builder);
    }

    @NotNull
    @Override
    public String getDescriptionId(ItemStack stack) {
        return Util.makeDescriptionId("machine", getTemplateId(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        // We need NBT data for the rest of this
        boolean sneaking = Screen.hasShiftDown();

        if (sneaking) {
            UnboundCompactMachineItem.getTemplate(stack).ifPresent(actualTemplate -> {
                final var roomDimensions = actualTemplate.dimensions();
                tooltip.add(Component.literal("Size: " + roomDimensions.toShortString()).withStyle(ChatFormatting.YELLOW));

                final var templateId = getTemplateId(stack);
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
        setTemplate(stack, RoomTemplate.NO_TEMPLATE);
        ICompactMachineItem.setColor(stack, 0xFFFFFFFF);
        return stack;
    }

    public static ItemStack forTemplate(ResourceLocation templateId, RoomTemplate template) {
        final var stack = new ItemStack(fromRegistry(), 1);
        setTemplate(stack, templateId);
        ICompactMachineItem.setColor(stack, template.color());

        MachineDataTagBuilder.empty()
                .template(templateId)
                .color(template.color())
                .writeToBlockData(stack);

        return stack;
    }

    private static ItemStack setTemplate(ItemStack stack, ResourceLocation templateId) {
        var tag = stack.getOrCreateTag();
        tag.putString(NBT_TEMPLATE_ID, templateId.toString());
        return stack;
    }

    @NotNull
    public static ResourceLocation getTemplateId(ItemStack stack) {
        if (!stack.hasTag()) return RoomTemplate.NO_TEMPLATE;

        final var tag = stack.getTag();
        if (tag == null || tag.isEmpty() || !tag.contains(NBT_TEMPLATE_ID))
            return RoomTemplate.NO_TEMPLATE;

        return new ResourceLocation(tag.getString(NBT_TEMPLATE_ID));
    }

    @NotNull
    public static Optional<RoomTemplate> getTemplate(ItemStack stack) {
        var template = getTemplateId(stack);
        if (!template.equals(RoomTemplate.NO_TEMPLATE)) {
            final var actualTemplate = RoomHelper.getTemplates().get(template);
            return Optional.ofNullable(actualTemplate);
        }

        return Optional.empty();
    }
}
