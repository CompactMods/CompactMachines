package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Rooms {

    public static final ResourceKey<Registry<RoomTemplate>> TEMPLATE_REG_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "room_templates"));

    public static final ResourceKey<Registry<RoomUpgrade>> ROOM_UPGRADES_REG_KEY = ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "room_upgrades"));

    public static Registry<RoomTemplate> getTemplates(MinecraftServer server) {
        final var regAccess = server.registryAccess();
        return regAccess.registryOrThrow(TEMPLATE_REG_KEY);
    }
}
