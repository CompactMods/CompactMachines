package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.core.Capabilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;

public class RoomChunkData implements IMachineRoom {
    private final LevelChunk chunk;

    public RoomChunkData(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Nonnull
    @Override
    public ChunkPos getChunk() {
        return chunk.getPos();
    }

    @Nonnull
    @Override
    public ServerLevel getLevel() {
        return (ServerLevel) chunk.getLevel();
    }

    @Nonnull
    @Override
    public IRoomCapabilities getCapabilityManager() {
        return this.chunk.getCapability(Capabilities.ROOM_CAPS).resolve().orElseThrow();
    }

}
