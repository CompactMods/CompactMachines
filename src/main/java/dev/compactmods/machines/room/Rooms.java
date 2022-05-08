package dev.compactmods.machines.room;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import javax.naming.OperationNotSupportedException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Rooms {
    public static ChunkPos createNew(MinecraftServer serv, RoomSize size, UUID owner) throws MissingDimensionException {

        CompactRoomData rooms = CompactRoomData.get(serv);
        var connections = MachineToRoomConnections.get(serv);
        final var compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (connections == null) {
            throw new MissingDimensionException("Could not load world saved data while creating new room.");
        }

        if (compactWorld == null)
            throw new MissingDimensionException();

        int nextPosition = rooms.getNextSpiralPosition();
        Vec3i location = MathUtil.getRegionPositionByIndex(nextPosition);

        int centerY = ServerConfig.MACHINE_FLOOR_Y.get() + (size.getInternalSize() / 2);
        BlockPos newCenter = MathUtil.getCenterWithY(location, centerY);

        // Generate a new machine room
        CompactStructureGenerator.generateCompactStructure(compactWorld, size, newCenter);

        ChunkPos machineChunk = new ChunkPos(newCenter);
        connections.registerRoom(machineChunk);

        try {
            rooms.createNew()
                    .owner(owner)
                    .size(size)
                    .chunk(machineChunk)
                    .register();
        } catch (OperationNotSupportedException e) {
            CompactMachines.LOGGER.warn(e);
        }

        return machineChunk;
    }

    public static boolean destroy(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        var roomData = CompactRoomData.get(server);
        if (!roomData.isRegistered(room)) {
            throw new NonexistentRoomException(room);
        }

        final var level = server.getLevel(Registration.COMPACT_DIMENSION);
        if (level == null)
            throw new MissingDimensionException();

        final var roomBounds = roomData.getBounds(room);
        final var innerBounds = roomBounds.deflate(1);

        final var states = level.getBlockStates(innerBounds)
                .collect(Collectors.toSet());

        final var nonAir = states.stream()
                .filter(state -> !state.isAir())
                .findAny();

        if (nonAir.isPresent()) {
            CompactMachines.LOGGER.error("Refusing to delete room at {}; non-air blocks exist inside the room. First match: {}", room, nonAir.get());
            return false;
        }

        // clear tunnel connection info
        final var tunnels = RoomTunnelData.getFile(server, room);
        final var filename = RoomTunnelData.getDataFilename(room);
        if (!tunnels.delete()) {
            CompactMachines.LOGGER.warn("Could not delete tunnel data for room {}; clearing the connection graph as an alternative.", room);
            CompactMachines.LOGGER.warn("Data file to delete: {}", filename);

            var td = RoomTunnelData.get(server, room);
            td.getGraph().clear();
            td.setDirty();
        } else {
            // File deletion successful, delete cached data
            final var compactDataCache = level.getDataStorage().cache;
            compactDataCache.remove(filename);
        }

        // reset everything for the room boundary
        BlockPos.betweenClosedStream(roomBounds.inflate(1))
                .forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));

        // Remove room registration
        roomData.remove(room);

        // Disconnect all machines
        var conns = MachineToRoomConnections.get(server);
        var d = CompactMachineData.get(server);

        var connected = conns.getMachinesFor(room);
        for (int mid : connected) {
            var location = d.getMachineLocation(mid);
            location.ifPresent(p -> {
                var pos = p.getBlockPosition();
                var l = p.level(server);
                if (l.getBlockEntity(pos) instanceof TunnelWallEntity tunn) {
                    tunn.disconnect();
                }
            });
        }

        conns.unregisterRoom(room);
        return true;
    }

    public static Stream<Integer> getConnectedMachines(MinecraftServer server, ChunkPos room) {
        try {
            var conns = MachineToRoomConnections.get(server);
            return conns.getMachinesFor(room).stream();
        } catch (MissingDimensionException e) {
            return Stream.empty();
        }
    }

    public static RoomSize sizeOf(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        return CompactRoomData.get(server)
                .getData(room)
                .getSize();
    }

    public static IDimensionalPosition getSpawn(MinecraftServer serv, ChunkPos room) throws MissingDimensionException {
        return CompactRoomData.get(serv).getSpawn(room);
    }

    public static boolean exists(MinecraftServer server, ChunkPos room) {
        try {
            return CompactRoomData.get(server).isRegistered(room);
        } catch (MissingDimensionException e) {
            return false;
        }
    }

    public static StructureTemplate getInternalBlocks(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        final var tem = new StructureTemplate();

        final var lev = server.getLevel(Registration.COMPACT_DIMENSION);
        final var data = CompactRoomData.get(server);
        final var roomInfo = data.getData(room);

        final var bounds = roomInfo.getRoomBounds();
        final int inside = roomInfo.getSize().getInternalSize();
        tem.fillFromWorld(lev, new BlockPos(bounds.minX, bounds.minY - 1, bounds.minZ),
                new Vec3i(inside, inside + 1, inside), false, null);

        return tem;
    }
}
