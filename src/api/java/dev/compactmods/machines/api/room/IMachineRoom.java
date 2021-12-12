package dev.compactmods.machines.api.room;

import javax.annotation.Nonnull;
import dev.compactmods.machines.api.tunnels.connection.IMachineTunnels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface IMachineRoom {

    @Nonnull
    ChunkPos getChunk();

    @Nonnull
    ServerLevel getLevel();

    @Nonnull
    IMachineTunnels getTunnels();

    @Nonnull
    IRoomCapabilities getCapabilities();
}
