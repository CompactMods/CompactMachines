package dev.compactmods.machines.api.tunnels.lifecycle;

import dev.compactmods.machines.api.tunnels.ITunnelInstance;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;

/**
 * Indicates that a tunnel has teardown tasks that are performed whenever a tunnel
 * is removed from a machine room's wall.
 */
public interface ITunnelTeardown<Tunn extends ITunnelInstance> {

    /**
     * Handle teardown of tunnel here.
     *
     * @param instance The tunnel instance being modified.
     * @param reason The reason the teardown is occurring.
     */
    void teardown(ITunnelPosition position, Tunn instance, TeardownReason reason);

}
