package com.robotgryphon.compactmachines.tunnels;

import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import net.minecraft.item.Item;

public class TunnelDefinition {
    public EnumTunnelType type;
    public Item item;

    public TunnelDefinition(EnumTunnelType type, Item item) {
        this.type = type;
        this.item = item;
    }

    public Item getItem() {
        return this.item;
    }

}
