package dev.compactmods.machines.forge.room;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.room.server.RoomServerHelper;
import net.minecraft.core.Registry;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ForgeRoomServerHelper extends RoomServerHelper {

    public static Registry<RoomTemplate> getTemplates() {
        final var serv = ServerLifecycleHooks.getCurrentServer();
        return getTemplates(serv);
    }
}
