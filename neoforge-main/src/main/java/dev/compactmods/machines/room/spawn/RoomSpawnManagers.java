package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.data.manager.CMKeyedDataFileManager;
import net.minecraft.server.MinecraftServer;

public class RoomSpawnManagers implements IRoomSpawnManagers {

    private final CMKeyedDataFileManager<String, SpawnManager> spawnManagers;

    public RoomSpawnManagers(MinecraftServer server) {
        this.spawnManagers = new CMKeyedDataFileManager<>(server, SpawnManager::new);
    }

    @Override
    public IRoomSpawnManager get(String roomCode) {
        return spawnManagers.data(roomCode);
    }

    @Override
    public void save() {
        spawnManagers.save();
    }
}
