package dev.compactmods.machines.server;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.room.attachment.RoomDataAttachments;
import dev.compactmods.machines.room.generation.ServerRoomGenerator;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.room.registry.ServerRoomRegistry;
import dev.compactmods.machines.room.spatial.MemoryGraphChunkManager;
import dev.compactmods.machines.room.spawn.ServerRoomSpawnManagers;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

class ServerCapabilities implements Saveable, IServerCapabilities {

    private final MinecraftServer server;
    private final RoomRegistry roomRegistry;
    private final ServerRoomSpawnManagers spawnManagers;
    private final RoomGenerator roomGenerator;
    private final RoomChunkManager roomChunkManager;

    /// Per-room data-attachment holders, loaded once and reused so mutations survive until saved.
    private final Map<String, RoomDataAttachments> roomData = new HashMap<>();

    ServerCapabilities(MinecraftServer server) {
        this.server = server;
        this.roomRegistry = new ServerRoomRegistry(server);
        this.spawnManagers = new ServerRoomSpawnManagers(server);
        this.roomGenerator = new ServerRoomGenerator(server, roomRegistry);
        this.roomChunkManager = new MemoryGraphChunkManager(server);
    }

    RoomDataAttachments roomData(String roomCode) {
        return roomData.computeIfAbsent(roomCode, code -> new RoomDataAttachments(server, code));
    }

    @Override
    public RoomRegistry roomRegistry() {
        return roomRegistry;
    }

    @Override
    public IRoomSpawnManagers spawnManagers() {
        return spawnManagers;
    }

    @Override
    public RoomGenerator roomGenerator() {
        return roomGenerator;
    }

    @Override
    public RoomChunkManager chunkManager() {
        return roomChunkManager;
    }

    public void save() {
        Stream.of(roomRegistry, roomGenerator, spawnManagers)
                .filter(Saveable.class::isInstance)
                .map(Saveable.class::cast)
                .forEach(Saveable::save);

        roomData.values().forEach(RoomDataAttachments::save);
    }

    static void register(RegisterCapabilitiesEvent ignored) {

        ServerCapability.registerVoid(RoomCapabilities.REGISTRY, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.roomRegistry();
        });

        ServerCapability.registerVoid(RoomCapabilities.GENERATOR, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.roomGenerator();
        });

        ServerCapability.registerVoid(RoomCapabilities.CHUNK_MANAGER, (server, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps.chunkManager();
        });

        RoomCapability.register(RoomCapabilities.ROOM_DATA_ATTACHMENTS, (server, room, _) -> {
            var caps = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return caps instanceof ServerCapabilities sc
                    ? sc.roomData(room.code())
                    : new RoomDataAttachments(server, room.code());
        });

        RoomCapability.register(RoomCapabilities.SPAWN_MANAGER, (server, room, _) -> {
            var data = server.getData(CompactMachinesServer.SERVER_CAPABILITIES);
            return data.spawnManagers().get(room);
        });
    }
}
