package dev.compactmods.machines.test.services;

import com.google.common.base.Predicates;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.IRoomApi;
import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import dev.compactmods.machines.server.CompactMachinesServer;
import net.minecraft.server.MinecraftServer;

import java.util.function.Predicate;

public class TestRoomApi implements IRoomApi {

    private final GraphChunkManager chunkManager;
    private final RoomRegistrar registrar;
    private final RoomSpawnManagers spawnManagers;

    public TestRoomApi() {
        this.registrar = new RoomRegistrar(CompactMachinesServer.currentServer());
        this.spawnManagers = new RoomSpawnManagers(registrar);
        this.chunkManager = new GraphChunkManager();
    }

    @Override
    public Predicate<String> roomCodeValidator() {
        return Predicates.alwaysTrue();
    }

    @Override
    public IRoomRegistrar registrar() {
        return registrar;
    }

    @Override
    public IRoomSpawnManager spawnManager(String roomCode) {
        return spawnManagers.get(roomCode);
    }

    @Override
    public IRoomChunkManager chunkManager() {
        return chunkManager;
    }

    @Override
    public IRoomChunks chunks(String roomCode) {
        return chunkManager.get(roomCode);
    }

    @Override
    public void save() {

    }
}
