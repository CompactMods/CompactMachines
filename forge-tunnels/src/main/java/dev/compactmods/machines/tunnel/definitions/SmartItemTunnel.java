package dev.compactmods.machines.tunnel.definitions;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityLookupTunnel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FastColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

// WIP do not register yet
public class SmartItemTunnel implements TunnelDefinition, CapabilityLookupTunnel {
    @Override
    public int ringColor() {
        return FastColor.ARGB32.color(255, 205, 143, 36);
    }

    @Override
    public ImmutableSet<Capability<?>> getSupportedCapabilities() {
        return ImmutableSet.of(ForgeCapabilities.ITEM_HANDLER);
    }

    public <T extends Capability<T>> LazyOptional<T> findCapability(
            MinecraftServer server, TunnelPosition tunnelPosition,
            IDimensionalBlockPosition targetPosition) {
        final var lev = targetPosition.level(server);
        if(!lev.isLoaded(targetPosition.getBlockPosition())) {
            return LazyOptional.empty();
        }

        return targetPosition.getBlockEntity(server)
                .map(be -> be.getCapability(ForgeCapabilities.ITEM_HANDLER, tunnelPosition.machineSide().getOpposite()))
                .orElse(LazyOptional.empty())
                .cast();
    }

}
