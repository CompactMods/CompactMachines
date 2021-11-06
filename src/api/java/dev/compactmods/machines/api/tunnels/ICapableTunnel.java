package dev.compactmods.machines.api.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * A base interface that marks a tunnel as supporting capabilities (items, fluids, etc.)
 */
public interface ICapableTunnel {

    @Nonnull
    <T> LazyOptional<T> getInternalCapability(ServerWorld compactWorld, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);

    @Nonnull
    <T> LazyOptional<T> getExternalCapability(ServerWorld world, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);

    Map<Capability<?>, LazyOptional<?>> rebuildCapabilityCache(ServerWorld compactLevel, BlockPos tunnelPos, BlockPos inside, @Nullable IDimensionalPosition external);
}
