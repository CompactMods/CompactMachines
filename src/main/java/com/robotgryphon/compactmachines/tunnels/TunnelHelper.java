package com.robotgryphon.compactmachines.tunnels;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TunnelHelper {
    @Nonnull
    public static Direction getNextDirection(Direction in) {
        switch (in) {
            case UP:
                return Direction.DOWN;

            case DOWN:
                return Direction.NORTH;

            case NORTH:
                return Direction.SOUTH;

            case SOUTH:
                return Direction.WEST;

            case WEST:
                return Direction.EAST;

            case EAST:
                return Direction.UP;
        }

        return Direction.UP;
    }

    @Nonnull
    public static Optional<TunnelDefinition> getTunnelDefinitionFromType(ResourceLocation id) {
        Optional<RegistryObject<TunnelRegistration>> first = Registrations.TUNNEL_TYPES.getEntries()
                .stream()
                .filter(t -> t.get().getRegistryName() == id)
                .findFirst();

        if (!first.isPresent())
            return Optional.empty();

        TunnelRegistration reg = first.get().get();
        return Optional.of(reg.getDefinition());
    }

    @Nonnull
    public static Optional<DimensionalPosition> getTunnelConnectedPosition(TunnelWallTile tunnel, EnumTunnelSide side) {
        switch (side) {
            case OUTSIDE:
                return tunnel.getConnectedPosition();

            case INSIDE:
                RegistryKey<World> world = Registrations.COMPACT_DIMENSION;
                BlockPos offsetInside = tunnel.getPos().offset(tunnel.getTunnelSide().getOpposite());

                DimensionalPosition pos = new DimensionalPosition(world, offsetInside);
                return Optional.of(pos);
        }

        return Optional.empty();
    }

    @Nonnull
    public static Optional<BlockState> getConnectedState(World world, TunnelWallTile twt, EnumTunnelSide side) {
        Optional<DimensionalPosition> connectedPosition = getTunnelConnectedPosition(twt, side);
        if(!connectedPosition.isPresent())
            return Optional.empty();

        if (world instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) world;

            DimensionalPosition dimensionalPosition = connectedPosition.get();

            Optional<ServerWorld> connectedWorld = dimensionalPosition.getWorld(sw);
            if (!connectedWorld.isPresent())
                return Optional.empty();

            ServerWorld csw = connectedWorld.get();
            BlockPos connectedPos = dimensionalPosition.getBlockPosition();

            BlockState state = csw.getBlockState(connectedPos);
            return Optional.of(state);
        }

        return Optional.empty();
    }
}
