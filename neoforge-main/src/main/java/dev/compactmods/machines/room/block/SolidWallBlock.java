package dev.compactmods.machines.room.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class SolidWallBlock extends ProtectedWallBlock {

    public static final BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");

    public SolidWallBlock(Properties props) {
        super(props);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(LIGHTS_ON, true)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(
                LIGHTS_ON
        );
    }

    //    @Override
//    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
//        return pos.getY() == 40;
//    }
}
