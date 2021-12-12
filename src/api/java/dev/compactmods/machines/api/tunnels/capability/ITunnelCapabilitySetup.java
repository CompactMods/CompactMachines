package dev.compactmods.machines.api.tunnels.capability;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;

/**
 * Indicates that a tunnel has set up tasks for when a tunnel is placed
 * inside a machine room.
 */
public interface ITunnelCapabilitySetup {

    /**
     * Handle initialization tasks for the tunnel's capabilities here.
     *
     * @param room The room the tunnel was added to.
     * @param added The connection being set up from room to machine.
     */
    void setupCapabilities(IMachineRoom room, ITunnelConnection added);
}
