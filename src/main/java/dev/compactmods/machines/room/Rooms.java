package dev.compactmods.machines.room;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.dimension.MissingDimensionException;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import javax.naming.OperationNotSupportedException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class Rooms {
    public static ChunkPos createNew(MinecraftServer serv, RoomSize size, UUID owner) throws MissingDimensionException {
        final var compactWorld = serv.getLevel(Dimension.COMPACT_DIMENSION);

        if (compactWorld == null)
            throw new MissingDimensionException();

        CompactRoomData rooms = CompactRoomData.get(compactWorld);

        int nextPosition = rooms.getNextSpiralPosition();
        Vec3i location = MathUtil.getRegionPositionByIndex(nextPosition);

        int centerY = ServerConfig.MACHINE_FLOOR_Y.get() + (size.getInternalSize() / 2);
        BlockPos newCenter = MathUtil.getCenterWithY(location, centerY);

        // Generate a new machine room
        CompactStructureGenerator.generateCompactStructure(compactWorld, size, newCenter);

        ChunkPos machineChunk = new ChunkPos(newCenter);
        try {
            rooms.createNew()
                    .owner(owner)
                    .size(size)
                    .chunk(machineChunk)
                    .register();
        } catch (OperationNotSupportedException e) {
            // room already registered somehow
            CompactMachines.LOGGER.warn(e);
        }

        return machineChunk;
    }

    // TODO - Revisit with furnace recipe
//    public static boolean destroy(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
//        final var compactDim = server.getLevel(Registration.COMPACT_DIMENSION);
//        if (compactDim == null)
//            throw new MissingDimensionException();
//
//        var roomData = CompactRoomData.get(compactDim);
//        if (!roomData.isRegistered(room)) {
//            throw new NonexistentRoomException(room);
//        }
//
//        final var roomBounds = roomData.getBounds(room);
//        final var innerBounds = roomBounds.deflate(1);
//
//        final var states = compactDim.getBlockStates(innerBounds)
//                .collect(Collectors.toSet());
//
//        final var nonAir = states.stream()
//                .filter(state -> !state.isAir())
//                .findAny();
//
//        if (nonAir.isPresent()) {
//            CompactMachines.LOGGER.error("Refusing to delete room at {}; non-air blocks exist inside the room. First match: {}", room, nonAir.get());
//            return false;
//        }
//
//        // clear tunnel connection info
//        final var tunnels = RoomTunnelData.getFile(server, room);
//        final var filename = RoomTunnelData.getDataFilename(room);
//        if (!tunnels.delete()) {
//            CompactMachines.LOGGER.warn("Could not delete tunnel data for room {}; clearing the connection graph as an alternative.", room);
//            CompactMachines.LOGGER.warn("Data file to delete: {}", filename);
//
//            var td = RoomTunnelData.forRoom(server, room);
//            td.getGraph().clear();
//            td.setDirty();
//        } else {
//            // File deletion successful, delete cached data
//            final var compactDataCache = compactDim.getDataStorage().cache;
//            compactDataCache.remove(filename);
//        }
//
//        // reset everything for the room boundary
//        BlockPos.betweenClosedStream(roomBounds.inflate(1))
//                .forEach(p -> compactDim.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
//
//        // Remove room registration
//        roomData.remove(room);
//
//        // Disconnect all machines
//        var conns = MachineToRoomConnections.forDimension(server);
//        var d = CompactMachineData.get(server);
//
//        var connected = conns.getMachinesFor(room);
//        for (int mid : connected) {
//            var location = d.getMachineLocation(mid);
//            location.ifPresent(p -> {
//                var pos = p.getBlockPosition();
//                var l = p.level(server);
//                if (l.getBlockEntity(pos) instanceof TunnelWallEntity tunn) {
//                    tunn.disconnect();
//                }
//            });
//        }
//
//        conns.unregisterRoom(room);
//        return true;
//    }

    public static Stream<IDimensionalBlockPosition> getConnectedMachines(MinecraftServer server, ChunkPos room) {
        return server.levelKeys().stream()
                .map(server::getLevel)
                .filter(Objects::nonNull)
                .filter(sl -> sl.getDataStorage().cache.containsKey(DimensionMachineGraph.DATA_KEY))
                .flatMap(sl -> {
                    final var graph = DimensionMachineGraph.forDimension(sl);
                    return graph.getMachinesFor(room).stream()
                        .map(bp -> new LevelBlockPosition(sl.dimension(), bp));
                });
    }

    public static RoomSize sizeOf(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);
        return CompactRoomData.get(compactDim)
                .getData(room)
                .getSize();
    }

    public static IDimensionalPosition getSpawn(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);
        return CompactRoomData.get(compactDim).getSpawn(room);
    }

    public static boolean exists(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);
        return CompactRoomData.get(compactDim).isRegistered(room);
    }

    public static StructureTemplate getInternalBlocks(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        final var tem = new StructureTemplate();

        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var bounds = roomInfo.getRoomBounds();
        final int inside = roomInfo.getSize().getInternalSize();
        tem.fillFromWorld(compactDim, new BlockPos(bounds.minX, bounds.minY - 1, bounds.minZ),
                new Vec3i(inside, inside + 1, inside), false, null);

        return tem;
    }

    public static void resetSpawn(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if(!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var centerPoint = Vec3.atCenterOf(roomInfo.getCenter());
        final var newSpawn = centerPoint.subtract(0, (roomInfo.getSize().getInternalSize() / 2f), 0);

        data.setSpawn(room, newSpawn);
    }

    public static Optional<String> getRoomName(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if(!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        return roomInfo.getName();
    }

    public static Optional<GameProfile> getOwner(MinecraftServer server, ChunkPos room) {
        if(!exists(server, room))
            return Optional.empty();

        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);
        final var data = CompactRoomData.get(compactDim);

        try {
            final CompactRoomData.RoomData roomInfo = data.getData(room);
            final var ownerUUID = roomInfo.getOwner();

            return server.getProfileCache().get(ownerUUID);
        } catch (NonexistentRoomException e) {
            return Optional.empty();
        }
    }

    public static void updateName(MinecraftServer server, ChunkPos room, String newName) throws NonexistentRoomException {
        if(!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        roomInfo.setName(newName);
        data.setDirty();
    }
}
