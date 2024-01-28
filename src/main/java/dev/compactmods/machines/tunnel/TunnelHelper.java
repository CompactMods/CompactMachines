package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.core.Constants;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TunnelHelper {
    private static final TicketType<ChunkPos> CM4_TUUNEL_LOAD_TYPE = TicketType.create(Constants.MOD_ID + ":tunnels", Comparator.comparingLong(ChunkPos::toLong));

    @Nonnull
    public static Direction getNextDirection(Direction in) {
        return switch (in) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.EAST;
            case EAST -> Direction.UP;
        };
    }

    public static Stream<Direction> getOrderedSides() {
        return Stream.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    }

    public static Optional<Direction> getNextDirection(Direction current, Set<Direction> used) {
        final var ordered = getOrderedSides().toList();
        final var found = ordered.indexOf(current);
        final var stream = Stream.generate(() -> ordered).flatMap(Collection::stream);

        return stream.skip(found + 1).filter(dir -> !used.contains(dir)).findFirst();
    }

    public static void setChunkMode(ServerLevel level, ChunkPos room, boolean forceLoad) {
        final var chunks = level.getChunkSource();
        level.setChunkForced(room.x, room.z, forceLoad);
        if (forceLoad)
            chunks.addRegionTicket(CM4_TUUNEL_LOAD_TYPE, room, 2, room);
        else
            chunks.removeRegionTicket(CM4_TUUNEL_LOAD_TYPE, room, 2, room);
        chunks.save(true);
    }
}
