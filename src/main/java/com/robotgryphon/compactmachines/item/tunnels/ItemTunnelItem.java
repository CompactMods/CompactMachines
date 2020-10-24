package com.robotgryphon.compactmachines.item.tunnels;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.tunnels.TunnelRegistration;

public class ItemTunnelItem extends TunnelItem {
    public ItemTunnelItem(Properties properties) {
        super(properties);
    }

    @Override
    public TunnelRegistration getDefinition() {
        return Registrations.ITEM_TUNNEL.get();
    }
}
