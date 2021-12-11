package dev.compactmods.machines.block.walls;

import dev.compactmods.machines.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public class SolidWallBlock extends ProtectedWallBlock {
    public SolidWallBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter world, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return pos.getY() == ServerConfig.MACHINE_FLOOR_Y.get();
    }
}
