package dev.compactmods.machines.api.tunnels.connection;

import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides information on positions and states a tunnel is connected to.
 * Accessed via a sided capability lookup on either the tunnel block or a machine block.
 */
public interface ITunnelConnection {


    /**
     * Gets the level the tunnel is connected to.
     * @return
     */
    @Nonnull
    ServerLevel level();

    @Nonnull
    BlockState state();

    /**
     * From a machine block, gets the directional offset of the block adjacent to the tunnel.
     * From a tunnel block, gets the side of the machine the tunnel is bound to.
     */
    @Nonnull
    Direction side();

    @Nonnull
    BlockPos position();
}
