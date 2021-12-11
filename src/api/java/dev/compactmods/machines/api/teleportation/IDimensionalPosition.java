package dev.compactmods.machines.api.teleportation;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface IDimensionalPosition {
    BlockPos getBlockPosition();

    Optional<ServerLevel> getWorld(MinecraftServer server);
}
