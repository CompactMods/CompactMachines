package dev.compactmods.machines.api.location;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface IDimensionalPosition {

    Vec3 getExactPosition();

    ResourceKey<Level> dimensionKey();
    ServerLevel level(MinecraftServer server);

    Optional<Vec3> getRotation();
}
