package dev.compactmods.machines.api.tunnels.capability;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public interface CapabilityLookupTunnel {

    ImmutableSet<Capability<?>> getSupportedCapabilities();

    <T extends Capability<T>> LazyOptional<T> findCapability(MinecraftServer server, TunnelPosition tunnelPosition, GlobalPos connectedPosition);
}
