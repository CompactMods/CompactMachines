package dev.compactmods.machines.tunnel;

import javax.annotation.Nonnull;
import java.util.stream.Stream;
import net.minecraft.core.Direction;

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
}
