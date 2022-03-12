package dev.compactmods.machines.api.tunnels.lifecycle;

import dev.compactmods.machines.api.tunnels.TunnelPosition;
import net.minecraft.core.Direction;

/**
 * Indicates that a tunnel has teardown tasks that are performed whenever a tunnel
 * is removed from a machine room's wall, or rotated in-place.
 */
public interface TunnelTeardownHandler<Instance extends TunnelInstance> extends InstancedTunnel<Instance> {

    /**
     * Handle tasks when a tunnel is being rotated on a machine room wall.
     *
     * @param instance The tunnel instance being rotated.
     * @param oldSide The previous side of the machine the tunnel was connected to.
     * @param newSide The upcoming side of the machine the tunnel will connect to.
     */
    default void onRotated(TunnelPosition position, Instance instance, Direction oldSide, Direction newSide) {
    }

    /**
     * Handle tasks when a tunnel is fully removed from a machine room wall.
     *
     * @param instance The tunnel instance being removed.
     */
    default void onRemoved(TunnelPosition position, Instance instance) {
    }
}
