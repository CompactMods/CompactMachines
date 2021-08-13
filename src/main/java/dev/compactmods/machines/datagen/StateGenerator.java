package dev.compactmods.machines.datagen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.reference.EnumMachineSize;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, CompactMachines.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Wall block model
        models().cubeAll("block/wall", modLoc("block/wall"));

        // Machine models
        for(EnumMachineSize size : EnumMachineSize.values()) {
            String sizeName = size.getName();

            models()
                    .cubeAll("block/machine/machine_" + sizeName, modLoc("block/machine/machine_" + sizeName));
        }
    }
}
