package dev.compactmods.machines.forgebuiltin.tunnel.definitions;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityLookupTunnel;
import net.minecraft.core.GlobalPos;
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
            GlobalPos targetPosition) {
        final var lev = server.getLevel(targetPosition.dimension());
        if(!lev.isLoaded(targetPosition.pos())) {
            return LazyOptional.empty();
        }

        return lev.getBlockEntity(targetPosition.pos())
                .getCapability(ForgeCapabilities.ITEM_HANDLER, tunnelPosition.machineSide().getOpposite())
                .cast();
    }

}
