package dev.compactmods.machines.api.tunnels;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public record TunnelPosition(@Nullable ServerLevel level, BlockPos pos, Direction side) {

}
