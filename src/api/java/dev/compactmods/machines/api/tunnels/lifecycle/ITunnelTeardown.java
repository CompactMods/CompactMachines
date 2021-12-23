package dev.compactmods.machines.api.tunnels.lifecycle;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;

/**
 * Indicates that a tunnel has teardown tasks that are performed whenever a tunnel
 * is removed from a machine room's wall.
 */
public interface ITunnelTeardown {

    /**
     * Handle teardown of tunnel here.
     *
     * @param room The room the tunnel was removed from.
     * @param position The location of the tunnel being removed.
     * @param removed The connection being torn down, from machine to room.
     * @param reason The reason the teardown is occurring.
     */
    void teardown(IMachineRoom room, ITunnelPosition position, ITunnelConnection removed, TeardownReason reason);

}
