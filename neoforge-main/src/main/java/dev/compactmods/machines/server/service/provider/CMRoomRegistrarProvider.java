package dev.compactmods.machines.server.service.provider;

import dev.compactmods.machines.api.room.registration.IRoomRegistrar;
import dev.compactmods.machines.api.server.service.RoomRegistrarProvider;
import dev.compactmods.machines.room.CMRoomRegistrar;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public class CMRoomRegistrarProvider implements RoomRegistrarProvider {
    @Override
    public @NotNull IRoomRegistrar makeServiceInstance(MinecraftServer server) {
        return new CMRoomRegistrar(server);
    }
}
