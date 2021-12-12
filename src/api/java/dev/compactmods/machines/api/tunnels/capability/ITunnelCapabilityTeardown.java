package dev.compactmods.machines.api.tunnels.capability;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;

/**
 * Indicates that a tunnel has teardown tasks that are performed whenever a tunnel
 * is removed from a machine room's wall.
 */
public interface ITunnelCapabilityTeardown {


    /**
     * Handle teardown of tunnel capabilities here.
     *
     * @param room The room the tunnel was removed from.
     * @param removed The connection being torn down, from machine to room.
     */
    void teardownCapabilities(IMachineRoom room, ITunnelConnection removed);

}
