package dev.compactmods.machines.room.server;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.Rooms;
import net.minecraft.core.Registry;
import net.minecraftforge.server.ServerLifecycleHooks;

public class RoomServerHelper {
    public static Registry<RoomTemplate> getTemplates() {
        return ServerLifecycleHooks.getCurrentServer()
                .registryAccess()
                .registryOrThrow(Rooms.TEMPLATE_REG_KEY);
    }
}
