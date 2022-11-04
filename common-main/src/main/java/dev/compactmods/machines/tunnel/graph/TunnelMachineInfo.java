package dev.compactmods.machines.tunnel.graph;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public record TunnelMachineInfo(BlockPos location, ResourceLocation type, IDimensionalBlockPosition machine, Direction side) {
}
