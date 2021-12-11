package dev.compactmods.machines.datagen;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.block.BlockCompactMachine;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.EnumMachineSize;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, CompactMachines.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Wall block model
        var wall = models().cubeAll("block/wall", modLoc("block/wall"));
        simpleBlock(Registration.BLOCK_SOLID_WALL.get(), wall);
        simpleBlock(Registration.BLOCK_BREAKABLE_WALL.get(), wall);

        // Machine models
        for(EnumMachineSize size : EnumMachineSize.values()) {
            String sizeName = size.getName();

            var mod = models()
                    .cubeAll("block/machine/machine_" + sizeName, modLoc("block/machine/machine_" + sizeName));

            simpleBlock(BlockCompactMachine.getBySize(size), ConfiguredModel.builder()
                    .modelFile(mod)
                    .build());
        }
    }
}
