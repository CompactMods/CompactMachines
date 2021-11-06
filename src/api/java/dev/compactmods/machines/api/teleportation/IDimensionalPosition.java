package dev.compactmods.machines.api.teleportation;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public interface IDimensionalPosition {
    BlockPos getBlockPosition();

    Optional<ServerWorld> getWorld(MinecraftServer server);
}
