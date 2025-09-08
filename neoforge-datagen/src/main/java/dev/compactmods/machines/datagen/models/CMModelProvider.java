package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.datagen.base.ModelAndStateGenerator;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

public abstract class CMModelProvider extends ModelProvider {
    public CMModelProvider(PackOutput packOutput) {
        super(packOutput, CompactMachines.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        final var cm = new ModelAndStateGenerator.CMBlockModelGenerators(
                blockModels.blockStateOutput,
                blockModels.itemModelOutput,
                blockModels.modelOutput
        );

        registerModels(cm, itemModels);
    }

    protected abstract void registerModels(ModelAndStateGenerator.CMBlockModelGenerators blockModels, ItemModelGenerators itemModels);
}