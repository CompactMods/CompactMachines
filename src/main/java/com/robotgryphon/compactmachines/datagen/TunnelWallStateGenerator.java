package com.robotgryphon.compactmachines.datagen;


import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;

public class TunnelWallStateGenerator extends BlockStateProvider {
    public TunnelWallStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, CompactMachines.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        Block block = Registrations.BLOCK_TUNNEL_WALL.get();

        for (Direction dir : Direction.values()) {

            String typedTunnelDirectional = "tunnels/" + dir.getString();
            models().cubeAll(typedTunnelDirectional, modLoc("block/" + typedTunnelDirectional));

            // If we ever do one-side has the texture again
            // int x = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
            // int y = dir.getAxis().isVertical() ? 0 : (((int) dir.getHorizontalAngle()) + 180) % 360;

            getVariantBuilder(block)
                    .partialState()
                    .with(TunnelWallBlock.CONNECTED_SIDE, dir)
                    .setModels(
                            ConfiguredModel.builder()
                                    .modelFile(models().getExistingFile(modLoc(typedTunnelDirectional)))
                                    .build()
                    );

        }
    }
}
