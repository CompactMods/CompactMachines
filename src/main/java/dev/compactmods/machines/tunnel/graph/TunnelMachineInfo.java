package dev.compactmods.machines.tunnel.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

public record TunnelMachineInfo(BlockPos location, ResourceLocation type, GlobalPos machine, Direction side) {
}
