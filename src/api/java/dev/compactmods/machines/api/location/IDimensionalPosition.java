package dev.compactmods.machines.api.location;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IDimensionalPosition {
    BlockPos getBlockPosition();

    Optional<ServerLevel> getWorld(MinecraftServer server);

    Optional<BlockState> getBlockState(MinecraftServer server);
}
