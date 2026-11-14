package dev.compactmods.machines.datagen.models;

import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class CMItemModelGenerators extends ItemModelGenerators {
    public CMItemModelGenerators(ItemModelOutput output, BiConsumer<Identifier, ModelInstance> accepter) {
        super(output, accepter);
    }

    @Override
    public void run() {
    }
}
