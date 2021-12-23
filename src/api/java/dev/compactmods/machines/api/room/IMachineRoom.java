package dev.compactmods.machines.api.room;

import javax.annotation.Nonnull;
import dev.compactmods.machines.api.tunnels.connection.IRoomTunnels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface IMachineRoom {

    @Nonnull
    ChunkPos getChunk();

    @Nonnull
    ServerLevel getLevel();

    @Nonnull
    IRoomTunnels getTunnels();

    @Nonnull
    IRoomCapabilities getCapabilityManager();
}
