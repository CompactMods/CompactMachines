package dev.compactmods.machines.tunnel;

import net.minecraft.core.Direction;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TunnelHelper {

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
}
