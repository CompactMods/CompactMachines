package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.item.IItemImportTunnel;

public class ItemImportTunnel extends TunnelDefinition implements IItemImportTunnel {
    @Override
    public int getTunnelRingColor() {
        return 0xffcd8f24;
    }

    @Override
    public int getTunnelIndicatorColor() {
        return IMPORT_COLOR;
    }
}
