package dev.compactmods.machines.api.tunnels;

import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class TunnelDefinition extends ForgeRegistryEntry<TunnelDefinition>
    implements IForgeRegistryEntry<TunnelDefinition>
{
    /**
     * The color of a non-indicator (the same color as the wall)
     */
    public static final int NO_INDICATOR_COLOR = 3751749;

    public static final int IMPORT_COLOR = 0xff2462cd;
    public static final int EXPORT_COLOR = 0xffe6a709;

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
