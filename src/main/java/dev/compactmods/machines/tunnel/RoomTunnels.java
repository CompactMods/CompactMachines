package dev.compactmods.machines.tunnel;

import java.util.stream.Stream;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.IRoomTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class RoomTunnels implements IRoomTunnels {
    /**
     * Registers a new tunnel applied to a position inside a machine room.
     *
     * @param tunnel   The type of tunnel being registered.
     * @param innerPos The position of the tunnel being registered.
     * @return True if successfully registered, false otherwise.
     */
    @Override
    public <T extends TunnelDefinition> boolean registerTunnel(T tunnel, BlockPos innerPos) {
        return false;
    }

    /**
     * Creates a stream of registered tunnels for a machine room.
     * May contain multiple tunnels - see the connection information's side and tunnel type
     * information to further filter, or if more information is known, use a different query.
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> getTunnels() {
        return null;
    }

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels - see the connection information's side and tunnel type
     * information to further filter, or if more information is known, use a different query.
     *
     * @param machinePos The machine the tunnel(s) are connected to
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> getTunnels(IDimensionalPosition machinePos) {
        return null;
    }

    /**
     * Creates a stream of registered tunnels, given a connected machine position and the side
     * of the machine the tunnel is connected to. May contain multiple tunnels, as tunnels can
     * handle individual tasks that are combined (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @param machinePos The machine the tunnel(s) are connected to
     * @param side       The side of the machine the tunnel(s) are connected to
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> getTunnelsForSide(IDimensionalPosition machinePos, Direction side) {
        return null;
    }
}
