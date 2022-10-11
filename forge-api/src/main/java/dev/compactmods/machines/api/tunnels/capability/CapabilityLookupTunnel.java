package dev.compactmods.machines.api.tunnels.capability;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public interface CapabilityLookupTunnel {

    ImmutableSet<Capability<?>> getSupportedCapabilities();

    <T extends Capability<T>> LazyOptional<T> findCapability(MinecraftServer server, TunnelPosition tunnelPosition, IDimensionalBlockPosition connectedPosition);
}
