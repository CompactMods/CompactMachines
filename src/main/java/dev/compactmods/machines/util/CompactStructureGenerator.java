package dev.compactmods.machines.util;

import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;

public class CompactStructureGenerator {

    public static AABB getWallBounds(RoomSize size, BlockPos cubeCenter, Direction wall) {
        int s = size.getInternalSize() / 2;

        BlockPos start;
        AABB wallBounds;

        boolean horiz = wall.getAxis().getPlane() == Direction.Plane.HORIZONTAL;
        if (horiz) {
            start = cubeCenter
                    .below(s)
                    .relative(wall, s + 1);

            wallBounds = new AABB(start, start)
                    .expandTowards(0, (s * 2) + 1, 0);
        } else {
            start = cubeCenter.relative(wall, s + 1);

            wallBounds = new AABB(start, start)
                    .inflate(s + 1, 0, s + 1);
        }

        switch (wall) {
            case NORTH:
            case SOUTH:
                wallBounds = wallBounds.inflate(s + 1, 0, 0);
                break;

            case WEST:
            case EAST:
                wallBounds = wallBounds.inflate(0, 0, s + 1);
                break;
        }

        return wallBounds;
    }
    /**
     * Generates a wall or platform in a given direction.
     *
     * @param world
     * @param size
     * @param cubeCenter
     * @param wallDirection
     */
    public static void generateCompactWall(LevelAccessor world, RoomSize size, BlockPos cubeCenter, Direction wallDirection) {
        final var unbreakableWall = Registration.BLOCK_SOLID_WALL.get().defaultBlockState();
        final var wallBounds = getWallBounds(size, cubeCenter, wallDirection);

        BlockPos.betweenClosedStream(wallBounds)
                .filter(world::isEmptyBlock)
                .map(BlockPos::immutable)
                .forEach(p -> world.setBlock(p, unbreakableWall, 7));
    }

    /**
     * Generates a machine "internal" structure in a world via a machine size and a central point.
     *
     * @param world
     * @param size
     * @param center
     */
    public static void generateCompactStructure(LevelAccessor world, RoomSize size, BlockPos center) {
        int s = size.getInternalSize() / 2;

        BlockPos floorCenter = center.relative(Direction.DOWN, s);
        AABB floorBlocks = new AABB(floorCenter, floorCenter)
                .inflate(s, 0, s);

        boolean anyAir = BlockPos.betweenClosedStream(floorBlocks).anyMatch(world::isEmptyBlock);

        if (anyAir) {
            // Generate the walls
            Arrays.stream(Direction.values())
                    .forEach(d -> generateCompactWall(world, size, center, d));
        }
    }
}
