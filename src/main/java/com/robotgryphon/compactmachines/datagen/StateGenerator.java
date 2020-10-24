package com.robotgryphon.compactmachines.datagen;

import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, CompactMachines.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }
}
