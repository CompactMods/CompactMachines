package dev.compactmods.machines.api.room;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;

public interface IRoomCapabilities {

    <T> void addCapability(Capability<T> capability, T instance, Direction side);

    <T> void removeCapability(Capability<T> capability, Direction side);
}
