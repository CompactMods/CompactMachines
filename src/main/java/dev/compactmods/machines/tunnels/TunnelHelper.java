package dev.compactmods.machines.tunnels;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TunnelHelper {

    public static ITunnelConnectionInfo generateConnectionInfo(TunnelWallTile tunnelTile) {
        return new TunnelConnectionInfo(tunnelTile);
    }

    public static ITunnelConnectionInfo generateConnectionInfo(@Nonnull IBlockReader tunnelWorld, @Nonnull BlockPos tunnelPos) {
        TunnelWallTile tile = (TunnelWallTile) tunnelWorld.getBlockEntity(tunnelPos);
        return generateConnectionInfo(tile);
    }

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
        Optional<RegistryObject<TunnelDefinition>> first = Registration.TUNNEL_DEFINITIONS.getEntries()
                .stream()
                .filter(t -> t.get().getRegistryName() == id)
                .findFirst();

        return first.map(RegistryObject::get);

    }

    public static Set<BlockPos> getTunnelsForMachineSide(int machine, ServerWorld world, Direction machineSide) {

        ServerWorld compactWorld = world.getServer().getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null)
            return Collections.emptySet();

        final MachineConnections machines = MachineConnections.get(world.getServer());
        final CompactRoomData rooms = CompactRoomData.get(world.getServer());
        if (machines == null || rooms == null)
            return Collections.emptySet();

        // TODO - Reimplement with capability - this is wasteful
        return machines.graph.getConnectedRoom(machine).map(roomChunk -> {
            return rooms.getInnerBounds(roomChunk)
                    .map(b -> b.inflate(1))
                    .map(BlockPos::betweenClosedStream).orElse(Stream.empty())
                    .filter(pos -> {
                        BlockState state = compactWorld.getBlockState(pos);
                        return state.getBlock() instanceof TunnelWallBlock;
                    })
                    .map(BlockPos::immutable)
                    .collect(Collectors.toSet());
        }).orElse(Collections.emptySet());

//        CompactMachineServerData data = SavedMachineData.getInstance(world.getServer()).getData();
//
//        Optional<CompactMachineRegistrationData> mData = data.getMachineData(machine);
//        mData.ifPresent(machineData -> {
//            BlockPos machineCenter = machineData.getCenter();
//            int internalSize = machineData.getSize().getInternalSize();
//
//            AxisAlignedBB allBlocksInMachine = new AxisAlignedBB(machineCenter, machineCenter)
//                    .inflate(internalSize);
//
//            Set<BlockPos> tunnelPositionsUnfiltered = BlockPos.betweenClosedStream(allBlocksInMachine)
//                    .filter(pos -> !compactWorld.isEmptyBlock(pos))
//                    // .filter(pos -> world.getBlockState(pos).getBlock() instanceof TunnelWallBlock)
//                    .map(BlockPos::immutable)
//                    .collect(Collectors.toSet());
//
//            if(!tunnelPositionsUnfiltered.isEmpty()) {
//                Set<BlockPos> tunnelPositionsFiltered = tunnelPositionsUnfiltered
//                        .stream()
//                        .filter(pos -> {
//                            BlockState state = compactWorld.getBlockState(pos);
//
//                            boolean tunnel = state.getBlock() instanceof TunnelWallBlock;
//                            if(!tunnel)
//                                return false;
//
//                            Direction externalSide = state.getValue(TunnelWallBlock.CONNECTED_SIDE);
//                            return externalSide == machineSide;
//                        })
//                        .map(BlockPos::immutable)
//                        .collect(Collectors.toSet());
//
//                tunnelPositions.addAll(tunnelPositionsFiltered);
//            }
//        });
    }

    @Nonnull
    public static Optional<IDimensionalPosition> getTunnelConnectedPosition(TunnelWallTile tunnel, EnumTunnelSide side) {
        switch (side) {
            case OUTSIDE:
                return tunnel.getConnectedPosition();

            case INSIDE:
                RegistryKey<World> world = Registration.COMPACT_DIMENSION;
                BlockPos offsetInside = tunnel.getBlockPos().relative(tunnel.getTunnelSide());

                DimensionalPosition pos = new DimensionalPosition(world, offsetInside);
                return Optional.of(pos);
        }

        return Optional.empty();
    }

    @Nonnull
    public static Optional<BlockState> getConnectedState(TunnelWallTile twt, EnumTunnelSide side) {
        IDimensionalPosition connectedPosition = getTunnelConnectedPosition(twt, side).orElse(null);
        if (connectedPosition == null)
            return Optional.empty();

        // We need a server world to reach across dimensions to get information
        if (twt.getLevel() instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) twt.getLevel();

            Optional<ServerWorld> connectedWorld = connectedPosition.getWorld(sw.getServer());
            if (!connectedWorld.isPresent())
                return Optional.empty();

            ServerWorld csw = connectedWorld.get();
            BlockPos connectedPos = connectedPosition.getBlockPosition();

            BlockState state = csw.getBlockState(connectedPos);
            return Optional.of(state);
        }

        return Optional.empty();
    }


}
