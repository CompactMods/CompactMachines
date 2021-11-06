package dev.compactmods.machines.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TunnelConnectionInfo implements ITunnelConnectionInfo {

    private final TunnelWallTile tunnel;

    TunnelConnectionInfo(TunnelWallTile tile) {
        this.tunnel = tile;
    }

    @Nonnull
    @Override
    public Optional<IDimensionalPosition> getConnectedPosition(EnumTunnelSide side) {
        return TunnelHelper.getTunnelConnectedPosition(tunnel, side);
    }

    @Override
    public Optional<BlockState> getConnectedState(EnumTunnelSide side) {
        return TunnelHelper.getConnectedState(tunnel, side);
    }

    @Override
    public Optional<? extends IWorldReader> getConnectedWorld(EnumTunnelSide side) {
        ServerPlayerEntity sp;
        switch(side) {
            case INSIDE:
                return Optional.ofNullable(tunnel.getLevel());

            case OUTSIDE:
                return tunnel.getConnectedPosition().map(p -> {
                    MinecraftServer serv = tunnel.getLevel().getServer();
                    return p.getWorld(serv).orElse(null);
                });
        }

        return Optional.empty();
    }

    /**
     * Gets the connection information for sidedness.
     * <p>
     * For inside the machine, gets the side the tunnel was placed on.
     * For outside the machine, gets the side of the machine the tunnel is connected to.
     *
     * @param side Which side of the tunnel to get directional info for.
     */
    @Override
    public Direction getConnectedSide(EnumTunnelSide side) {
        switch(side) {
            case INSIDE:
                return tunnel.getTunnelSide();
            case OUTSIDE:
                return tunnel.getConnectedSide();
        }

        return Direction.UP;
    }
}
