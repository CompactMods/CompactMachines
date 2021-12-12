package dev.compactmods.machines.api.tunnels.connection;

import java.util.stream.Stream;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Contract interface for information regarding a machine room's tunnels.
 */
public interface IRoomTunnels {

    /**
     * Registers a new tunnel applied to a position inside a machine room.
     *
     * @param tunnel The type of tunnel being registered.
     * @param innerPos The position of the tunnel being registered.
     * @param <T> Tunnel type. Must extend {@link TunnelDefinition}.
     * @return True if successfully registered, false otherwise.
     */
    <T extends TunnelDefinition> boolean registerTunnel(T tunnel, BlockPos innerPos);

    /**
     * Creates a stream of registered tunnels for a machine room.
     * May contain multiple tunnels - see the connection information's side and tunnel type
     * information to further filter, or if more information is known, use a different query.
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    Stream<ITunnelConnection> getTunnels();

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels - see the connection information's side and tunnel type
     * information to further filter, or if more information is known, use a different query.
     *
     * @param machinePos The machine the tunnel(s) are connected to
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    Stream<ITunnelConnection> getTunnels(IDimensionalPosition machinePos);

    /**
     * Creates a stream of registered tunnels, given a connected machine position and the side
     * of the machine the tunnel is connected to. May contain multiple tunnels, as tunnels can
     * handle individual tasks that are combined (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @param machinePos The machine the tunnel(s) are connected to
     * @param side The side of the machine the tunnel(s) are connected to
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    Stream<ITunnelConnection> getTunnelsForSide(IDimensionalPosition machinePos, Direction side);
}

