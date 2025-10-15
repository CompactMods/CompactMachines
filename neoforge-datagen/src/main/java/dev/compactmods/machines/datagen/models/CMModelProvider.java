package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.api.CompactMachines;
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
        final var cmBlocks = new CMBlockModelGenerators(
                blockModels.blockStateOutput,
                blockModels.itemModelOutput,
                blockModels.modelOutput
        );

        final var cmItems = new CMItemModelGenerators(itemModels.itemModelOutput, itemModels.modelOutput);

        registerModels(cmBlocks, cmItems);

        cmBlocks.run();
        cmItems.run();
    }

    protected abstract void registerModels(CMBlockModelGenerators blockModels, CMItemModelGenerators itemModels);
}