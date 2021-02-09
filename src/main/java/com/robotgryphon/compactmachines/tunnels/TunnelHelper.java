package com.robotgryphon.compactmachines.tunnels;

import com.robotgryphon.compactmachines.api.tunnels.EnumTunnelSide;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        Optional<RegistryObject<TunnelDefinition>> first = Registration.TUNNEL_TYPES.getEntries()
                .stream()
                .filter(t -> t.get().getRegistryName() == id)
                .findFirst();

        return first.map(RegistryObject::get);

    }

    public static Set<BlockPos> getTunnelsForMachineSide(int machine, ServerWorld world, Direction machineSide) {

        ServerWorld compactWorld = world.getServer().getWorld(Registration.COMPACT_DIMENSION);

        Set<BlockPos> tunnelPositions = new HashSet<>();

        CompactMachineServerData data = CompactMachineServerData.getInstance(world.getServer());

        Optional<CompactMachineRegistrationData> mData = data.getMachineData(machine);
        mData.ifPresent(machineData -> {
            BlockPos machineCenter = machineData.getCenter();
            int internalSize = machineData.getSize().getInternalSize();

            AxisAlignedBB allBlocksInMachine = new AxisAlignedBB(machineCenter, machineCenter)
                    .grow(internalSize);

            Set<BlockPos> tunnelPositionsUnfiltered = BlockPos.getAllInBox(allBlocksInMachine)
                    .filter(pos -> !compactWorld.isAirBlock(pos))
                    // .filter(pos -> world.getBlockState(pos).getBlock() instanceof TunnelWallBlock)
                    .map(BlockPos::toImmutable)
                    .collect(Collectors.toSet());

            if(!tunnelPositionsUnfiltered.isEmpty()) {
                Set<BlockPos> tunnelPositionsFiltered = tunnelPositionsUnfiltered
                        .stream()
                        .filter(pos -> {
                            BlockState state = compactWorld.getBlockState(pos);

                            boolean tunnel = state.getBlock() instanceof TunnelWallBlock;
                            if(!tunnel)
                                return false;

                            Direction externalSide = state.get(TunnelWallBlock.CONNECTED_SIDE);
                            return externalSide == machineSide;
                        })
                        .map(BlockPos::toImmutable)
                        .collect(Collectors.toSet());

                tunnelPositions.addAll(tunnelPositionsFiltered);
            }
        });

        return tunnelPositions;
    }
    @Nonnull
    public static Optional<DimensionalPosition> getTunnelConnectedPosition(TunnelWallTile tunnel, EnumTunnelSide side) {
        switch (side) {
            case OUTSIDE:
                return tunnel.getConnectedPosition();

            case INSIDE:
                RegistryKey<World> world = Registration.COMPACT_DIMENSION;
                BlockPos offsetInside = tunnel.getPos().offset(tunnel.getTunnelSide());

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
