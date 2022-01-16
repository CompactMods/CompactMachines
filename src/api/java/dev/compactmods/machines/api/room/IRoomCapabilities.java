package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * References all the tunnel-backed capabilities inside a machine room.
 */
public interface IRoomCapabilities {

    <CapType, TunnType extends TunnelDefinition>
    void addCapability(TunnType tunnel, Capability<CapType> capability, CapType instance, Direction side);

    <CapType, TunnType extends TunnelDefinition>
    void removeCapability(TunnType tunnel, Capability<CapType> capability, Direction side);

    <CapType, TunnType extends TunnelDefinition>
    LazyOptional<CapType> getCapability(TunnType tunnType, Capability<CapType> capability, Direction side);
}
