package dev.compactmods.machines.room.server;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.Rooms;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

public class RoomServerHelper {
    public static Registry<RoomTemplate> getTemplates(MinecraftServer server) {
        return server
                .registryAccess()
                .registryOrThrow(Rooms.TEMPLATE_REG_KEY);
    }
}
