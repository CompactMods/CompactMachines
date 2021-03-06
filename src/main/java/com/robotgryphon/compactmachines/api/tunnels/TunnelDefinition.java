package com.robotgryphon.compactmachines.api.tunnels;

import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class TunnelDefinition extends ForgeRegistryEntry<TunnelDefinition>
{
    /**
     * The color of a non-indicator (the same color as the wall)
     */
    public static final int NO_INDICATOR_COLOR = 3751749;

    public abstract int getTunnelRingColor();

    /**
     * Gets the color for the indicator at the top-right of the block texture.
     *
     * @return
     */
    public int getTunnelIndicatorColor() {
        return NO_INDICATOR_COLOR;
    }

}
