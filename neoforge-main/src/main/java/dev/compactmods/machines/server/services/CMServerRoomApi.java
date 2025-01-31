package dev.compactmods.machines.server.services;

import dev.compactmods.machines.api.room.IRoomApi;
import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.data.manager.CMSingletonDataFileManager;
import dev.compactmods.machines.room.RoomRegistrar;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import dev.compactmods.machines.server.CompactMachinesServer;

import java.util.function.Predicate;

public class CMServerRoomApi implements IRoomApi {

    private final CMSingletonDataFileManager<RoomRegistrar> ROOM_REGISTRAR_DATA;
    private final GraphChunkManager GRAPH_CHUNK_MANAGER;
    private final RoomSpawnManagers SPAWN_MANAGER;

    public CMServerRoomApi() {
        final var server = CompactMachinesServer.currentServer();
        ROOM_REGISTRAR_DATA = new CMSingletonDataFileManager<>(server, "room_registrations", new RoomRegistrar(server));
        ROOM_REGISTRAR_DATA.load();

        final var ROOM_REGISTRAR = ROOM_REGISTRAR_DATA.data();
        SPAWN_MANAGER = new RoomSpawnManagers(ROOM_REGISTRAR);

        GRAPH_CHUNK_MANAGER = new GraphChunkManager();
        ROOM_REGISTRAR.allRooms().forEach(inst -> GRAPH_CHUNK_MANAGER.calculateChunks(inst.code(), inst.boundaries()));
    }
    @Override
    public Predicate<String> roomCodeValidator() {
        return registrar()::isRegistered;
    }

    @Override
    public IRoomRegistrar registrar() {
        return ROOM_REGISTRAR_DATA.data();
    }

    @Override
    public IRoomSpawnManager spawnManager(String roomCode) {
        return SPAWN_MANAGER.get(roomCode);
    }

    @Override
    public IRoomChunkManager chunkManager() {
        return GRAPH_CHUNK_MANAGER;
    }

    @Override
    public IRoomChunks chunks(String roomCode) {
        return GRAPH_CHUNK_MANAGER.get(roomCode);
    }

    @Override
    public void save() {
        ROOM_REGISTRAR_DATA.save();
    }
}
