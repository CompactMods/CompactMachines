package com.robotgryphon.compactmachines.api.tunnels;

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
    <T> LazyOptional<T> getInternalCapability(ServerWorld compactWorld, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);

    @Nonnull
    <T> LazyOptional<T> getExternalCapability(ServerWorld world, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);
}
