package dev.compactmods.machines.api.tunnels.lifecycle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface InstancedTunnel<T extends TunnelInstance> {

    /**
     * Handle initialization tasks for the tunnel's data here.
     *
     * @param position The location of the new tunnel being created.
     * @param side The side of the wall the tunnel is being added to.
     */
    T newInstance(BlockPos position, Direction side);
}
