package dev.compactmods.machines.room.spawn;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.core.data.manager.CodecFileManager;
import dev.compactmods.machines.room.data.CMRoomDataLocations;
import net.minecraft.server.MinecraftServer;

public final class ServerRoomSpawnManagers implements IRoomSpawnManagers {

    private final CodecFileManager.Keyed<RoomInstance, SpawnManager> spawnManagers;

    public ServerRoomSpawnManagers(MinecraftServer server) {
        this.spawnManagers = CodecFileManager.keyed(RoomInstance::code, SpawnManager.CODEC)
                .at(CMRoomDataLocations.PLAYER_SPAWNS)
                .build(server);
    }

    public IRoomSpawnManager get(RoomInstance room) {
        final SpawnManager manager;
        if (this.spawnManagers.hasData(room)) {
            manager = this.spawnManagers.data(room);
        } else {
            manager = new SpawnManager(room);
            this.spawnManagers.set(room, manager);
            this.spawnManagers.save();
        }

        // Spawn changes mutate the cached manager in place; the keyed file manager only writes
        // files marked dirty, so mark it and write it now — spawns change rarely.
        manager.onChanged(() -> {
            this.spawnManagers.markDirty(room);
            this.spawnManagers.save();
        });
        return manager;
    }

    public void save() {
        this.spawnManagers.save();
    }
}
