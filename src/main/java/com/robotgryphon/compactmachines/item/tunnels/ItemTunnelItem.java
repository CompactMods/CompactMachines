package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;

public class ItemTunnelItem extends TunnelItem {
    public ItemTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public TunnelDefinition getDefinition() {
        return Registrations.ITEM_TUNNEL.get();
    }
}
