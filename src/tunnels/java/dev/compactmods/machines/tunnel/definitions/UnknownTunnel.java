package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;

public class UnknownTunnel implements TunnelDefinition {

    @Override
    public int ringColor() {
        return NO_INDICATOR_COLOR;
    }

}
