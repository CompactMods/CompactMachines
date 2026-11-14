package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;

public interface IServerCapabilities {
    RoomRegistry roomRegistry();

    IRoomSpawnManagers spawnManagers();

    RoomGenerator roomGenerator();

    RoomChunkManager chunkManager();
}
