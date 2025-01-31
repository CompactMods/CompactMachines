package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;

import java.util.function.Predicate;

public interface IRoomApi {

    Predicate<String> roomCodeValidator();

    IRoomRegistrar registrar();

    IRoomSpawnManager spawnManager(String roomCode);

    IRoomChunkManager chunkManager();

    IRoomChunks chunks(String roomCode);

    void save();
}
