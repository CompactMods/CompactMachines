package com.robotgryphon.compactmachines.tunnels;

import net.minecraft.util.Direction;

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
}
