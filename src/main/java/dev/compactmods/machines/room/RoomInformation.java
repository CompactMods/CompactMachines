package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.IRoomInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public record RoomInformation(ServerLevel level, ChunkPos chunk, RoomSize size)
        implements IRoomInformation {
}
