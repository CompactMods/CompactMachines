package dev.compactmods.machines.api.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ITunnelConnectionInfo {
    @Nonnull
    Optional<IDimensionalPosition> getConnectedPosition(EnumTunnelSide side);

    @Nonnull
    Optional<BlockState> getConnectedState(EnumTunnelSide side);

    Optional<? extends LevelReader> getConnectedWorld(EnumTunnelSide side);

    /**
     * Gets the connection information for sidedness.
     *
     * For inside the machine, gets the side the tunnel was placed on.
     * For outside the machine, gets the side of the machine the tunnel is connected to.
     *
     * @param side Which side of the tunnel to get directional info for.
     */
    Direction getConnectedSide(EnumTunnelSide side);
}
