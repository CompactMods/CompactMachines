package dev.compactmods.machines.api.tunnels.connection;

import java.util.stream.Stream;

/**
 * Machine-accessible tunnel information. Depending on how the information is queried from a
 * machine, this provides either all tunnel information for the machine, or information about
 * a connected side of a machine.
 */
public interface IMachineTunnels {

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels, as tunnels can handle individual tasks
     * that are combined in one block (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    Stream<ITunnelConnection> getTunnels();
}
