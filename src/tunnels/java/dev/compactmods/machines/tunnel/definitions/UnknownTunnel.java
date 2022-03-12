package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class UnknownTunnel extends ForgeRegistryEntry<TunnelDefinition> implements TunnelDefinition {

    @Override
    public int ringColor() {
        return NO_INDICATOR_COLOR;
    }

}
