package dev.compactmods.machines.api.tunnels.lifecycle;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;

/**
 * Indicates that a tunnel has set up tasks for when a tunnel is placed
 * inside a machine room.
 */
public interface ITunnelSetup {

    /**
     * Handle initialization tasks for the tunnel's data here.
     *
     * @param room The room the tunnel was added to.
     * @param added The connection being set up from room to machine.
     */
    void setup(IMachineRoom room, ITunnelPosition tunnel, ITunnelConnection added);
}
