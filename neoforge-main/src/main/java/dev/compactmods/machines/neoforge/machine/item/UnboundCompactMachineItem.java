package dev.compactmods.machines.neoforge.machine.item;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.Tooltips;
import dev.compactmods.machines.api.machine.item.IUnboundCompactMachineItem;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.machine.Machines;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Represents a machine item that has not been bound to a room yet,
 * but has an assigned template to use.
 */
public class UnboundCompactMachineItem extends BlockItem implements IUnboundCompactMachineItem {

    public static final String NBT_TEMPLATE_ID = "template_id";

    public UnboundCompactMachineItem(Properties builder) {
        super(Machines.UNBOUND_MACHINE_BLOCK.get(), builder);
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

        if (sneaking && worldIn != null) {
            getTemplate(worldIn.registryAccess(), stack).ifPresent(actualTemplate -> {
                final var roomDimensions = actualTemplate.internalDimensions();
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

    @NotNull
    public static Optional<RoomTemplate> getTemplate(RegistryAccess registries, ItemStack stack) {
        if(stack.getItem() instanceof IUnboundCompactMachineItem unbound) {
            var template = unbound.getTemplateId(stack);
            if (!template.equals(RoomTemplate.NO_TEMPLATE)) {
                final var actualTemplate = registries.registryOrThrow(RoomTemplate.REGISTRY_KEY).get(template);
                return Optional.ofNullable(actualTemplate);
            }
        }

        return Optional.empty();
    }

    public static ItemStack forTemplate(ResourceLocation templateId, RoomTemplate template) {
        final var stack = new ItemStack(Machines.UNBOUND_MACHINE_BLOCK_ITEM.get(), 1);
        if(stack.getItem() instanceof IUnboundCompactMachineItem unbound) {
            unbound.setTemplate(stack, templateId);
            unbound.setColor(stack, template.color());
        }

        final var tag = stack.getOrCreateTag();
        tag.putString(NBT_TEMPLATE_ID, templateId.toString());
        return stack;
    }

}
