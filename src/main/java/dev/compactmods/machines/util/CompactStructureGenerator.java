package dev.compactmods.machines.util;

import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.RoomSize;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;

import java.util.Random;

import static dev.compactmods.machines.CompactMachines.MOD_ID;

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
    public static void generateCompactStructure(ServerLevelAccessor world, RoomSize size, BlockPos center) {
        var machine = world.getLevel().getStructureManager().get(new ResourceLocation(MOD_ID, size.getName())).get();
        int s = (int) Math.ceil((size.getInternalSize() / 2) + 1);
        machine.placeInWorld(world, center.subtract(new BlockPos(s, s, s)), BlockPos.ZERO, new StructurePlaceSettings(), new Random(), Block.UPDATE_ALL);
    }
}
