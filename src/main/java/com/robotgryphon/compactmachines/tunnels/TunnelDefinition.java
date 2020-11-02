package com.robotgryphon.compactmachines.tunnels;

import net.minecraft.item.Item;

public abstract class TunnelDefinition {
    public Item item;

    public TunnelDefinition(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return this.item;
    }

    public abstract int getTunnelColor();
}
