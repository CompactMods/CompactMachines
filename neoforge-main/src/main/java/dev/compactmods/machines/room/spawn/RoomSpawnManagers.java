package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;

public class RoomSpawnManagers implements IRoomSpawnManagers {

    private final HashMap<String, IRoomSpawnManager> spawnManagers;

    public RoomSpawnManagers(MinecraftServer server) {
        this.spawnManagers = new HashMap<>();

        CompactMachines.roomRegistrar()
                .allRooms()
                .forEach(roomInstance -> {
                    final var manager = new SpawnManager(roomInstance.code(), roomInstance.boundaries());
                    spawnManagers.put(roomInstance.code(), manager);
                });
    }

    @Override
    public IRoomSpawnManager get(String roomCode) {
        return spawnManagers.computeIfAbsent(roomCode, (code) -> CompactMachines
                .room(roomCode)
                .map(inst -> new SpawnManager(roomCode, inst.boundaries()))
                .orElseThrow());
    }

    @Override
    public void save() {
        // FIXME
    }
}
