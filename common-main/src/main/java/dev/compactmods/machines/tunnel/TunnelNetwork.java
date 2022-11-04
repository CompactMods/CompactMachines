package dev.compactmods.machines.tunnel;

import net.minecraft.resources.ResourceLocation;

// TODO: Implementation

/**
 * A tunnel network helps to abstract storage and implementation details for both
 * tunnels and upgrades, away from individual blocks and systems.
 * For example, a pump upgrade for a machine may require insertion of fluids
 * into a room's fluid storage.
 */
public class TunnelNetwork {

    public static TunnelNetwork forRoomAndType(String room, ResourceLocation networkType) {
        return null;
    }
}
