package dev.compactmods.machines.util;

import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.EnumMachineSize;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Arrays;

public class CompactStructureGenerator {
    /**
     * Generates a wall or platform in a given direction.
     *
     * @param world
     * @param size
     * @param cubeCenter
     * @param wallDirection
     */
    public static void generateCompactWall(IWorld world, EnumMachineSize size, BlockPos cubeCenter, Direction wallDirection) {
        int s = size.getInternalSize() / 2;

        BlockState unbreakableWall = Registration.BLOCK_SOLID_WALL.get().defaultBlockState();

        BlockPos start = BlockPos.ZERO;
        AxisAlignedBB wallBounds;

        boolean horiz = wallDirection.getAxis().getPlane() == Direction.Plane.HORIZONTAL;
        if (horiz) {
            start = cubeCenter
                    .below(s)
                    .relative(wallDirection, s + 1);

            wallBounds = new AxisAlignedBB(start, start)
                    .expandTowards(0, (s * 2) + 1, 0);
        } else {
            start = cubeCenter.relative(wallDirection, s + 1);

            wallBounds = new AxisAlignedBB(start, start)
                    .inflate(s + 1, 0, s + 1);
        }

        switch (wallDirection) {
            case NORTH:
            case SOUTH:
                wallBounds = wallBounds.inflate(s + 1, 0, 0);
                break;

            case WEST:
            case EAST:
                wallBounds = wallBounds.inflate(0, 0, s + 1);
                break;
        }

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
    public static void generateCompactStructure(IWorld world, EnumMachineSize size, BlockPos center) {
        int s = size.getInternalSize() / 2;

        BlockPos floorCenter = center.relative(Direction.DOWN, s);
        AxisAlignedBB floorBlocks = new AxisAlignedBB(floorCenter, floorCenter)
                .inflate(s, 0, s);

        boolean anyAir = BlockPos.betweenClosedStream(floorBlocks).anyMatch(world::isEmptyBlock);

        if (anyAir) {
            // Generate the walls
            Arrays.stream(Direction.values())
                    .forEach(d -> generateCompactWall(world, size, center, d));
        }
    }
}
