package dev.compactmods.machines.api.room;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;

public interface IRoomInformation {

    @Nonnull
    ChunkPos chunk();

    @Nonnull
    ServerLevel level();
}
