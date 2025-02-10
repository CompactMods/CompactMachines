package dev.compactmods.machines.server.service.provider;

import dev.compactmods.machines.api.room.spawn.IRoomSpawnManagers;
import dev.compactmods.machines.api.server.service.RoomSpawnManagersProvider;
import dev.compactmods.machines.room.spawn.RoomSpawnManagers;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class CMRoomSpawnManagersProvider implements RoomSpawnManagersProvider {
    @Override
    public @NotNull IRoomSpawnManagers makeServiceInstance(MinecraftServer server) {
        return new RoomSpawnManagers(server);
    }
}
