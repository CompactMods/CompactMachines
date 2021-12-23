package dev.compactmods.machines.api.location;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public interface IDimensionalPosition {
    IDimensionalPosition relative(Direction direction, float amount);

    BlockPos getBlockPosition();

    Optional<ServerLevel> level(MinecraftServer server);

    Optional<BlockState> state(MinecraftServer server);

    IDimensionalPosition relative(Direction direction);
}
