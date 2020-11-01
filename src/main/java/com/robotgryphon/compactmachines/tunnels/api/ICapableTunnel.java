package com.robotgryphon.compactmachines.tunnels.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;

/**
 * A base interface that marks a tunnel as supporting capabilities (items, fluids, etc.)
 */
public interface ICapableTunnel {

    @Nonnull
    <T> LazyOptional<T> getCapability(ServerWorld world, BlockState state, BlockPos pos, @Nonnull Capability<T> cap, Direction side);
}
