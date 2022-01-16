package dev.compactmods.machines.tunnel;

import javax.annotation.Nullable;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public record TunnelPosition(@Nullable ServerLevel level, BlockPos pos, Direction side) implements ITunnelPosition {

}
