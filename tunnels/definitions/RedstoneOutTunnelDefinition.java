package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneWriterTunnel;

import java.awt.Color;

public class RedstoneOutTunnelDefinition extends TunnelDefinition implements IRedstoneWriterTunnel {
    @Override
    public int getStrongPower(ITunnelConnectionInfo connectionInfo) {
        return 0;
    }

    @Override
    public int getWeakPower(ITunnelConnectionInfo connectionInfo) {
        return 0;
    }

    @Override
    public int getTunnelRingColor() {
        return new Color(167, 38, 38).getRGB();
    }
}
