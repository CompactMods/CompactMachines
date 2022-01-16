package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.ITunnel;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

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

    @Override
    public UnknownTunnel.Instance newInstance(BlockPos pos, Direction side) {
        return new Instance();
    }

    public static class Instance implements ITunnel {
    }
}
