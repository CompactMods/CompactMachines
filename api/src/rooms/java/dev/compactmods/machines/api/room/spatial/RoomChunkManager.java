package dev.compactmods.machines.api.room.spatial;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.world.level.ChunkPos;

import java.util.Optional;

public interface RoomChunkManager {

    void initializeCache();
    
    void calculateChunks(String roomCode, RoomBoundaries boundaries);

    Optional<RoomInstance> findRoomByChunk(ChunkPos chunk);

    IRoomChunks get(String room);
}
