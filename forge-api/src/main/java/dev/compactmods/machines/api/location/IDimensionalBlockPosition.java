package dev.compactmods.machines.api.location;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public interface IDimensionalBlockPosition extends IDimensionalPosition {
    BlockPos getBlockPosition();

    Optional<BlockEntity> getBlockEntity(MinecraftServer server);

    BlockState state(MinecraftServer server);

    IDimensionalBlockPosition relative(Direction direction);
}
