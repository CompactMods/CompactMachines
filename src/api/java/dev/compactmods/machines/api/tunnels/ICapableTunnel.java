package dev.compactmods.machines.api.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
    <T> LazyOptional<T> getInternalCapability(ServerLevel compactWorld, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);

    @Nonnull
    <T> LazyOptional<T> getExternalCapability(ServerLevel world, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side);

    Map<Capability<?>, LazyOptional<?>> rebuildCapabilityCache(ServerLevel compactLevel, BlockPos tunnelPos, BlockPos inside, @Nullable IDimensionalPosition external);
}
