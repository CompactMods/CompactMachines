package dev.compactmods.machines.room;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.naming.OperationNotSupportedException;
import java.util.UUID;
import java.util.stream.Collectors;

public class Rooms {
    public static ChunkPos createNew(MinecraftServer serv, RoomSize size, UUID owner) throws MissingDimensionException {

        CompactRoomData rooms = CompactRoomData.get(serv);
        var connections = MachineToRoomConnections.get(serv);
        final var compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (connections == null) {
            throw new MissingDimensionException("Could not load world saved data while creating new room.");
        }

        if(compactWorld == null)
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

        final var states = level.getBlockStates(innerBounds).collect(Collectors.toSet());
        final var nonAir = states.stream()
                .filter(state -> !state.isAir())
                .findAny();

        if (nonAir.isPresent()) {
            CompactMachines.LOGGER.error("Refusing to delete room at {}; non-air blocks exist inside the room. First match: {}", room, nonAir.get());
            return false;
        }

        // clear tunnel connection info
        final var tunnels = RoomTunnelData.get(server, room);
        final var tGraph = tunnels.getGraph();
        tGraph.clear();
        tunnels.setDirty();

        // reset everything for the room boundary
        BlockPos.betweenClosedStream(roomBounds)
                .forEach(p -> level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));

        // Remove room registration
        roomData.remove(room);

        // Disconnect all machines
        var conns = MachineToRoomConnections.get(server);
        var d = CompactMachineData.get(server);

        var connected = conns.getMachinesFor(room);
        for(int mid : connected) {
            d.getMachineLocation(mid);
        }

        conns.unregisterRoom(room);
        return true;
    }
}
