package dev.compactmods.machines.api.core;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class CMRegistries {

    public static final ResourceKey<Registry<TunnelDefinition>> TYPES_REG_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(MOD_ID, "tunnel_types"));

    public static ResourceKey<Registry<RoomTemplate>> TEMPLATE_REG_KEY =
            ResourceKey.createRegistryKey(new ResourceLocation(MOD_ID, "room_templates"));

    public static final ResourceKey<Registry<RoomUpgrade>> ROOM_UPGRADES_REG_KEY = ResourceKey
            .createRegistryKey(new ResourceLocation(MOD_ID, "room_upgrades"));
}
