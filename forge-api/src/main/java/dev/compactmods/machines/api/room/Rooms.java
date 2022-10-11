package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.core.CMRegistries;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

public class Rooms {

    public static Registry<RoomTemplate> getTemplates(MinecraftServer server) {
        final var regAccess = server.registryAccess();
        return regAccess.registryOrThrow(CMRegistries.TEMPLATE_REG_KEY);
    }
}
