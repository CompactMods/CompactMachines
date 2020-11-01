package com.robotgryphon.compactmachines.tunnels;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Optional;

public class TunnelHelper {
    public static Direction getNextDirection(Direction in) {
        switch(in) {
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

            default: return null;
        }
    }

    @Nonnull
    public static Optional<TunnelDefinition> getTunnelDefinitionFromType(EnumTunnelType enumTunnelType) {
        Optional<RegistryObject<TunnelRegistration>> first = Registrations.TUNNEL_TYPES.getEntries()
                .stream()
                .filter(t -> t.get().getType() == enumTunnelType)
                .findFirst();

        if(!first.isPresent())
            return Optional.empty();

        TunnelRegistration reg = first.get().get();
        return Optional.ofNullable(reg.getDefinition());
    }

    public static Optional<BlockState> getConnectedState(World world, TunnelWallTile twt) {
        Optional<DimensionalPosition> connectedPosition = twt.getConnectedPosition();
        if (!connectedPosition.isPresent())
            return Optional.empty();

        if(world instanceof ServerWorld) {
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
