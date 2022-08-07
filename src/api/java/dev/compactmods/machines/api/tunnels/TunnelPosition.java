package dev.compactmods.machines.api.tunnels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record TunnelPosition(BlockPos pos, Direction wallSide, Direction machineSide) {

}
