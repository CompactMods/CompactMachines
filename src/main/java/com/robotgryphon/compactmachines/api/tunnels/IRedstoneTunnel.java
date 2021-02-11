package com.robotgryphon.compactmachines.api.tunnels;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public interface IRedstoneTunnel {
    boolean canConnectRedstone(IBlockReader world, BlockState state, BlockPos pos, Direction side);

    boolean canProvidePower(BlockState state);

    int getStrongPower(IBlockReader world, BlockState state, BlockPos pos, Direction side);

    int getWeakPower(IBlockReader world, BlockState state, BlockPos pos, Direction side);
}
