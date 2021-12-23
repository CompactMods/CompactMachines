package dev.compactmods.machines.api.tunnels;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public interface ITunnelPosition {

    @Nonnull
    ServerLevel level();

    @Nonnull
    BlockPos pos();

    @Nonnull
    Direction side();
}
