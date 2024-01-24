package dev.compactmods.machines.api.location;

import java.util.Optional;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IDimensionalPosition {

    BlockPos getBlockPosition();
    Vec3 getExactPosition();

    ResourceKey<Level> dimensionKey();
    ServerLevel level(MinecraftServer server);

    IDimensionalPosition relative(Direction direction);

    Optional<Vec3> getRotation();

    boolean isLoaded(MinecraftServer serv);
}
