package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;

public class UnknownTunnel extends TunnelDefinition {
    /**
     * The central ring color of the tunnel. Shown in the tunnel item and on blocks.
     *
     * @return An AARRGGBB-formatted integer indicating color.
     */
    @Override
    public int getTunnelRingColor() {
        return NO_INDICATOR_COLOR;
    }
}
