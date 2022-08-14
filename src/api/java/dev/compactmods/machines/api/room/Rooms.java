package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

public class Rooms {

    public static ResourceKey<Registry<RoomTemplate>> TEMPLATE_REG_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(Constants.MOD_ID, "room_templates"));

    public static Registry<RoomTemplate> getTemplates(MinecraftServer server) {
        final var regAccess = server.registryAccess();
        return regAccess.registryOrThrow(TEMPLATE_REG_KEY);
    }
}
