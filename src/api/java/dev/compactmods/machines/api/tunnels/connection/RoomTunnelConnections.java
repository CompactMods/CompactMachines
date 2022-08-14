package dev.compactmods.machines.api.tunnels.connection;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.stream.Stream;

public interface RoomTunnelConnections {
    /**
     * Fetches sided redstone tunnel locations inside a machine room, based on connected machine
     * location and the side the tunnel is pointed to.
     * @param machine The connected machine (non-compact level)
     * @param facing The side the tunnels are queried for
     * @return
     */
    Stream<BlockPos> getRedstoneTunnels(IDimensionalBlockPosition machine, Direction facing);
}
