package dev.compactmods.machines.forge.machine.item;

import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.forge.room.RoomHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MachineItemUtil {
    public static final String NBT_TEMPLATE_ID = MachineNbt.NBT_TEMPLATE_ID;
    public static final String NBT_CUSTOM_NAME = "custom_name";

    public static ItemStack setTemplate(ItemStack stack, ResourceLocation templateId) {
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

    public static Optional<String> getMachineName(ItemStack stack) {
        if(!stack.hasTag()) return Optional.empty();

        final var tag = stack.getTag();
        if(!tag.contains(NBT_CUSTOM_NAME, Tag.TAG_STRING))
            return Optional.empty();

        return Optional.of(tag.getString(NBT_CUSTOM_NAME));
    }
}
