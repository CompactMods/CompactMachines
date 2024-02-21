package dev.compactmods.machines.neoforge.tunnel.graph.traversal;

import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.neoforge.tunnel.Tunnels;
import dev.compactmods.machines.tunnel.graph.traversal.ITunnelFilter;
import dev.compactmods.machines.tunnel.graph.traversal.TunnelTypeFilters;
import net.neoforged.common.capabilities.Capability;

public class ForgeTunnelTypeFilters {

    public static ITunnelFilter capability(Capability<?> capability) {
        return TunnelTypeFilters.definition(def -> {
            if (!(def instanceof CapabilityTunnel<?> tcp))
                return false;

            return tcp.getSupportedCapabilities().contains(capability);
        }, Tunnels::getDefinition);
    }
}
