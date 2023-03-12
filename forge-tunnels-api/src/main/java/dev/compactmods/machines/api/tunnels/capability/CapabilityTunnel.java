package dev.compactmods.machines.api.tunnels.capability;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.tunnels.lifecycle.InstancedTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface CapabilityTunnel<Tunnel extends TunnelInstance> extends InstancedTunnel<Tunnel> {

    ImmutableSet<Capability<?>> getSupportedCapabilities();

    /**
     * Fetch a capability instance from a tunnel.
     * @param type Capability type. See implementations like {@link IItemHandler} as a reference.
     * @param <CapType> Type of capability to fetch off tunnel.
     * @return LazyOptional instance of the capability, or LO.empty otherwise.
     */
    <CapType> LazyOptional<CapType> getCapability(Capability<CapType> type, Tunnel instance);

}
