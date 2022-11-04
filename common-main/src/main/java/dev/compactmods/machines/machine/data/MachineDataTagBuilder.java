package dev.compactmods.machines.machine.data;

import com.google.gson.JsonObject;
import dev.compactmods.machines.api.machine.MachineEntityNbt;
import dev.compactmods.machines.api.machine.MachineIds;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.api.room.RoomTemplate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Used for setting up data that a Compact Machine block entity can
 * read during its data loading process.
 */
public class MachineDataTagBuilder {

    private ResourceLocation templateId;
    private int color;

    private MachineDataTagBuilder() {
        this.templateId = RoomTemplate.NO_TEMPLATE;
        this.color = RoomTemplate.INVALID_TEMPLATE.color();
    }

    public static MachineDataTagBuilder empty() {
        return new MachineDataTagBuilder();
    }

    public static MachineDataTagBuilder forTemplate(ResourceLocation templateId, RoomTemplate template) {
        return new MachineDataTagBuilder()
                .template(templateId)
                .color(template.color());
    }

    public MachineDataTagBuilder color(int color) {
        this.color = color;
        return this;
    }

    public MachineDataTagBuilder template(ResourceLocation templateId) {
        this.templateId = templateId;
        return this;
    }

    public MachineDataTagBuilder writeToItem(ItemStack stack) {
        final var tag = stack.getOrCreateTag();
        tag.putInt(MachineEntityNbt.NBT_CUSTOM_COLOR, color);
        tag.putString(MachineEntityNbt.NBT_TEMPLATE_ID, templateId.toString());
        return this;
    }

    public MachineDataTagBuilder writeToItemJson(JsonObject json) {
        json.addProperty(MachineNbt.NBT_TEMPLATE_ID, templateId.toString());
        json.addProperty(MachineNbt.NBT_COLOR, color);
        return this;
    }

    public MachineDataTagBuilder writeToBlockData(ItemStack stack) {
        final var data = writeBlockDataTag();
        data.putString("id", MachineIds.BLOCK_ENTITY.toString());
        stack.addTagElement("BlockEntityTag", data);
        return this;
    }

    public MachineDataTagBuilder writeToBlockDataJson(JsonObject json) {
        if(json.has("BlockEntityTag"))
            json.remove("BlockEntityTag");

        json.add("BlockEntityTag", writeBlockDataJson());
        return this;
    }

    private CompoundTag writeBlockDataTag() {
        final var tag = new CompoundTag();
        tag.putString(MachineEntityNbt.NBT_TEMPLATE_ID, templateId.toString());
        tag.putInt(MachineEntityNbt.NBT_CUSTOM_COLOR, color);
        return tag;
    }

    private JsonObject writeBlockDataJson() {
        final var tag = new JsonObject();
        tag.addProperty("id", MachineIds.BLOCK_ENTITY.toString());
        tag.addProperty(MachineEntityNbt.NBT_TEMPLATE_ID, templateId.toString());
        tag.addProperty(MachineEntityNbt.NBT_CUSTOM_COLOR, color);
        return tag;
    }


}
