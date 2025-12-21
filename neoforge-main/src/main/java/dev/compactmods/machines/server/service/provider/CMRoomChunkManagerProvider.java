package dev.compactmods.machines.server.service.provider;

import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.server.service.RoomChunkManagerProvider;
import dev.compactmods.machines.room.spatial.GraphChunkManager;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class CMRoomChunkManagerProvider implements RoomChunkManagerProvider {
    @Override
    public @NotNull IRoomChunkManager makeServiceInstance(MinecraftServer server) {
        return new GraphChunkManager(server);
    }
}
