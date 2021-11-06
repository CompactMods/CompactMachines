package dev.compactmods.machines.api.tunnels.redstone;

import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;

/**
 * A redstone writer sends a redstone value from inside the machine (redstone connected to a tunnel).
 */
public interface IRedstoneWriterTunnel extends IRedstoneTunnel {

    int getStrongPower(ITunnelConnectionInfo connectionInfo);

    int getWeakPower(ITunnelConnectionInfo connectionInfo);
}
