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

    /**
     * Constant value used to indicate that a tunnel is receiving a resource from
     * outside a machine room.
     */
    public static final int IMPORT_COLOR = 0xff2462cd;

    /**
     * Constant value used to indicate that a tunnel is pushing a resource out of
     * a machine room.
     */
    public static final int EXPORT_COLOR = 0xffe6a709;

    /**
     * The central ring color of the tunnel. Shown in the tunnel item and on blocks.
     * @return An AARRGGBB-formatted integer indicating color.
     */
    public abstract int getTunnelRingColor();

    /**
     * Gets the color for the indicator at the top-right of the block texture.
     * For import- and export-style tunnels, see {@link #IMPORT_COLOR} and {@link #EXPORT_COLOR}.
     *
     * @return An AARRGGBB-formatted integer indicating color.
     */
    public int getTunnelIndicatorColor() {
        return NO_INDICATOR_COLOR;
    }

}
