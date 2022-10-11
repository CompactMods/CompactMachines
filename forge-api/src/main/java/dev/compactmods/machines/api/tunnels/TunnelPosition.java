package dev.compactmods.machines.api.tunnels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Tunnel position information, for the tunnel placed inside a machine room.
 *
 * @param pos Absolute position inside the Compact dimension.
 * @param wallSide Side of the wall the tunnel is on.
 * @param machineSide Side of the machine the tunnel connects to.
 */
public record TunnelPosition(BlockPos pos, Direction wallSide, Direction machineSide) {

}
