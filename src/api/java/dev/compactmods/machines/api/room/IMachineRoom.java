package dev.compactmods.machines.api.room;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;

public interface IMachineRoom {

    @Nonnull
    ChunkPos getChunk();

    @Nonnull
    ServerLevel getLevel();
}
