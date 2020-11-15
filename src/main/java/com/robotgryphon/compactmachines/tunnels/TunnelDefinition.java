package com.robotgryphon.compactmachines.tunnels;

import net.minecraft.item.Item;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class TunnelDefinition extends ForgeRegistryEntry<TunnelDefinition> {
    protected Item item;

    /**
     * The color of a non-indicator (the same color as the wall)
     */
    public static final int NO_INDICATOR_COLOR = 3751749;

    public TunnelDefinition(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return this.item;
    }

    public abstract int getTunnelRingColor();

    /**
     * Gets the color for the indicator at the top-right of the block texture.
     *
     * @return
     */
    public abstract int getTunnelIndicatorColor();
}
