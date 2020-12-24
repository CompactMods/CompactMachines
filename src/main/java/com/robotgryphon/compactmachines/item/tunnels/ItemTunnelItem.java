package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;

public class ItemTunnelItem extends TunnelItem {
    public ItemTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public TunnelDefinition getDefinition() {
        return Registration.ITEM_TUNNEL.get();
    }
}
