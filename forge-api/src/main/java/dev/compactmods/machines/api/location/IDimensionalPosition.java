package dev.compactmods.machines.api.location;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IDimensionalPosition {

    Vec3 position();

    ResourceKey<Level> dimension();
    ServerLevel level(MinecraftServer server);

    boolean isLoaded(MinecraftServer serv);

    ChunkPos chunkPos();
}
