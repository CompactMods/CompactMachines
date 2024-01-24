package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class StateGenerator extends BlockStateProvider {
    public StateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, Constants.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Wall block model
        var wall = models().cubeAll("block/wall", modLoc("block/wall"));
        simpleBlock(Walls.BLOCK_SOLID_WALL.get(), wall);
        simpleBlock(Walls.BLOCK_BREAKABLE_WALL.get(), wall);

        // Machine models
        for(RoomSize size : RoomSize.values()) {
            String sizeName = size.getName();

            var mod = models()
                    .cubeAll("block/machine/machine_" + sizeName, modLoc("block/machine/machine_" + sizeName));

            simpleBlock(CompactMachineBlock.getBySize(size), ConfiguredModel.builder()
                    .modelFile(mod)
                    .build());
        }
    }
}
