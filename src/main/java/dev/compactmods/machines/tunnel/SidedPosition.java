package dev.compactmods.machines.tunnel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record SidedPosition(BlockPos pos, Direction side) {
}
