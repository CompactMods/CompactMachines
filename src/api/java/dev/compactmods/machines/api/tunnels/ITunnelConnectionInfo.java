package dev.compactmods.machines.api.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;
import java.util.Optional;

public interface ITunnelConnectionInfo {
    @Nonnull
    Optional<IDimensionalPosition> getConnectedPosition(EnumTunnelSide side);

    @Nonnull
    Optional<BlockState> getConnectedState(EnumTunnelSide side);

    Optional<? extends IWorldReader> getConnectedWorld(EnumTunnelSide side);

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
