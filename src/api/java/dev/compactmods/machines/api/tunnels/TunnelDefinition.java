package dev.compactmods.machines.api.tunnels;

import net.minecraftforge.registries.IForgeRegistryEntry;

public interface TunnelDefinition extends IForgeRegistryEntry<TunnelDefinition> {
    /**
     * The color of a non-indicator (the same color as the wall)
     */
    int NO_INDICATOR_COLOR = 3751749;

    /**
     * Constant value used to indicate that a tunnel is receiving a resource from
     * outside a machine room.
     */
    int IMPORT_COLOR = 0xff2462cd;

    /**
     * Constant value used to indicate that a tunnel is pushing a resource out of
     * a machine room.
     */
    int EXPORT_COLOR = 0xffe6a709;

    /**
     * The central ring color of the tunnel. Shown in the tunnel item and on blocks.
     *
     * @return An AARRGGBB-formatted integer indicating color.
     */
    int ringColor();

    /**
     * Gets the color for the indicator at the top-right of the block texture.
     * For import- and export-style tunnels, see {@link #IMPORT_COLOR} and {@link #EXPORT_COLOR}.
     *
     * @return An AARRGGBB-formatted integer indicating color.
     */
    default int indicatorColor() {
        return NO_INDICATOR_COLOR;
    }

}
