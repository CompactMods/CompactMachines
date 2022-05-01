package dev.compactmods.machines.api.location;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;

public interface IDimensionalBlockPosition extends IDimensionalPosition {
    BlockState state(MinecraftServer server);
}
