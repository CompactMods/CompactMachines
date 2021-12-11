package dev.compactmods.machines.api.tunnels;

import javax.annotation.Nonnull;
import java.util.Optional;
import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides information on positions and states a tunnel is connected to.
 * Accessed via a sided capability lookup on either the tunnel block or a machine block.
 */
public interface ITunnelConnection {

    @Nonnull
    IDimensionalPosition getConnectedPosition();

    @Nonnull
    BlockState getConnectedState();

    Optional<? extends LevelReader> getConnectedWorld();

    /**
     * Gets the connection information for sidedness.
     *
     * From a machine block, gets the direction of the block adjacent to the tunnel.
     * From a tunnel block, gets the side of the machine the tunnel is bound to.
     */
    Direction getConnectedSide();
}
