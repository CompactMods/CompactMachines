package dev.compactmods.machines.api.room;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * References all the tunnel-backed capabilities inside a machine room.
 */
public interface IRoomCapabilities {

    <CapType> LazyOptional<CapType> getCapability(Capability<CapType> capability, Direction side);
}
