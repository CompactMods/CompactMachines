package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.RoomCoreModel;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class CMItemModelGenerators extends ItemModelGenerators {
    public CMItemModelGenerators(ItemModelOutput output, BiConsumer<ResourceLocation, ModelInstance> accepter) {
        super(output, accepter);
    }

    @Override
    public void run() {
    }

    public void generateRoomCoreItem(ItemLike item) {
        final var unbaked = ItemModelUtils.specialModel(CompactMachines.modRL("room_core"), new RoomCoreModel.Unbaked());
        this.itemModelOutput.accept(item.asItem(), unbaked);
    }
}
