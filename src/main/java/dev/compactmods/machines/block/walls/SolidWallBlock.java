package dev.compactmods.machines.block.walls;

import dev.compactmods.machines.config.ServerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SolidWallBlock extends ProtectedWallBlock {
    public SolidWallBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return pos.getY() == ServerConfig.MACHINE_FLOOR_Y.get();
    }
}
