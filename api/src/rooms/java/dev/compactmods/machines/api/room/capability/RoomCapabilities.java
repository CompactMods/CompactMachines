package dev.compactmods.machines.api.room.capability;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.capability.ServerCapability;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.api.room.spatial.RoomChunkManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public interface RoomCapabilities {

    RoomCapability<IRoomSpawnManager, Void> SPAWN_MANAGER = createVoidCap("spawn_manager", IRoomSpawnManager.class);

    RoomCapability<IAttachmentHolder, Void> ROOM_DATA_ATTACHMENTS = createVoidCap("data_attachments", IAttachmentHolder.class);

    ServerCapability<RoomRegistry, Void> REGISTRY = createServer("registrar", RoomRegistry.class);

    ServerCapability<RoomGenerator, Void> GENERATOR = createServer("generator", RoomGenerator.class);

    ServerCapability<RoomChunkManager, Void> CHUNK_MANAGER = createServer("chunk_manager", RoomChunkManager.class);

    private static <T> RoomCapability<T, Void> createVoidCap(String id, Class<T> type) {
        return RoomCapability.createVoid(CompactMachinesCore.identifier(id), type);
    }

    private static <T> ServerCapability<T, Void> createServer(String id, Class<T> type) {
        return ServerCapability.createVoid(CompactMachinesCore.identifier(id), type);
    }
}
