package dev.compactmods.machines.util;

import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CompactStructureGenerator {

    public static AABB getWallBounds(Vec3i size, BlockPos cubeFloor, Direction wall) {
        BlockPos start;

        switch (wall) {
            case NORTH, SOUTH -> {
                int offsetNorthSouth = (int) Math.ceil(size.getZ() / 2f);
                start = cubeFloor.relative(wall,  offsetNorthSouth);

                return new AABB(start, start)
                        .expandTowards(0, size.getY(), 0)
                        .inflate(Math.ceil(size.getX() / 2f), 0, 0);
            }

            case WEST, EAST -> {
                final var offsetWestEast = (int) Math.ceil(size.getX() / 2f);
                start = cubeFloor.relative(wall,  offsetWestEast);

                return new AABB(start, start)
                        .expandTowards(0, size.getY(), 0)
                        .inflate(0, 0, Math.ceil(size.getZ() / 2f));
            }

            case UP, DOWN -> {
                start = wall == Direction.DOWN ? cubeFloor : cubeFloor.relative(wall, size.getY());
                var aabb = new AABB(start, start)
                        .inflate(Math.ceil(size.getX() / 2f), 0, Math.ceil(size.getZ() / 2f));

                if(wall == Direction.UP)
                    aabb = aabb.inflate(0, 1, 0);

                return aabb;
            }
        }

        // catch-all
        return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    }

    /**
     * Generates a wall or platform in a given direction.
     *
     * @param world
     * @param dimensions
     * @param cubeCenter
     * @param wallDirection
     */
    public static void generateCompactWall(LevelAccessor world, Vec3i dimensions, BlockPos cubeCenter, Direction wallDirection) {
        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        final var wallBounds = getWallBounds(dimensions, cubeCenter, wallDirection);

        BlockPos.betweenClosedStream(wallBounds)
                // .filter(world::isEmptyBlock)
                .map(BlockPos::immutable)
                .forEach(p -> world.setBlock(p, unbreakableWall, 7));
    }

    /**
     * Generates a wall or platform in a given direction.
     *
     * @param world
     * @param size
     * @param cubeFloor
     * @param wallDirection
     */
    @Deprecated(forRemoval = true)
    public static void generateCompactWall(LevelAccessor world, RoomSize size, BlockPos cubeFloor, Direction wallDirection) {
        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        final var wallBounds = getWallBounds(size.toVec3(), cubeFloor, wallDirection);

        BlockPos.betweenClosedStream(wallBounds)
                .filter(world::isEmptyBlock)
                .map(BlockPos::immutable)
                .forEach(p -> world.setBlock(p, unbreakableWall, 7));
    }

    /**
     * Generates a machine "internal" structure in a world via a machine size and a central point.
     *
     * @param world
     * @param dimensions Internal dimensions of the room.
     * @param cubeFloorCenter
     */
    public static void generateCompactStructure(LevelAccessor world, Vec3i dimensions, BlockPos cubeFloorCenter) {
        AABB floorBlocks = getWallBounds(dimensions, cubeFloorCenter, Direction.DOWN);
        AABB machineInternal = floorBlocks
                .move(1, 1, 1)
                .contract(2, 0, 2)
                .expandTowards(0, dimensions.getY() - 1, 0);


        boolean anyAir = BlockPos.betweenClosedStream(floorBlocks).anyMatch(world::isEmptyBlock);

        if (anyAir) {
            // Generate the walls
            for(final var dir : Direction.values())
                generateCompactWall(world, dimensions, cubeFloorCenter, dir);

            // Clear out the inside of the room
            BlockPos.betweenClosedStream(machineInternal)
                    .forEach(p -> world.setBlock(p, Blocks.AIR.defaultBlockState(), 7));

        }
    }

    public static BlockPos cornerFromSize(Vec3i dimensions, BlockPos cubeFloorCenter) {
        Vec3i offset = new Vec3i(
                -Math.floor(dimensions.getX() / 2f),
                1,
                -Math.floor(dimensions.getZ() / 2f)
        );

        return cubeFloorCenter.offset(offset);
    }
}
