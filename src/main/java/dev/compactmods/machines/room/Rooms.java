package dev.compactmods.machines.room;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.stream.Collectors;

public class Rooms {
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

        // TODO - Ensure no tunnels in room

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
        return true;
    }
}
