package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.WallConstants;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import dev.compactmods.spatial.aabb.AABBAligner;
import dev.compactmods.spatial.aabb.AABBHelper;
import dev.compactmods.spatial.vector.VectorUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;

public class CompactRoomGenerator {

    /**
     * Generates a wall or platform in a given direction.
     * Uses the solid wall block.
     *
     * @param world
     * @param outerBounds
     * @param wallDirection
     * @since 3.0.0
     */
    public static void generateCompactWall(LevelAccessor world, AABB outerBounds, Direction wallDirection, BlockState block) {
        AABB wallBounds = BlockSpaceUtil.getWallBounds(outerBounds, wallDirection);
        BlockSpaceUtil.blocksInside(wallBounds).forEach(wallBlock -> {
            world.setBlock(wallBlock, block, Block.UPDATE_ALL);
        });
    }

    /**
     * Generates a machine "internal" structure in a world via a machine size and a central point.
     *
     * @param world
     * @param outerBounds Outer dimensions of the room.
     */
    public static void generateRoom(LevelAccessor world, AABB outerBounds) {
        final var block = BuiltInRegistries.BLOCK.getValue(WallConstants.SOLID_WALL);
        if (block != null) {
            final var solidWall = block.defaultBlockState();
            generateRoom(world, outerBounds, solidWall);
        }
    }

    /**
     * Generates a machine structure in a world via machine boundaries and a wall block.
     *
     * @param world
     * @param outerBounds Outer dimensions of the room.
     * @param block       Block to use for walls.
     */
    public static void generateRoom(LevelAccessor world, AABB outerBounds, BlockState block) {

        // Generate the walls
        for (final var dir : Direction.values())
            generateCompactWall(world, outerBounds, dir, block);

        // Clear out the inside of the room
        AABB machineInternal = outerBounds.deflate(1);
        BlockSpaceUtil.blocksInside(machineInternal)
                .forEach(p -> world.setBlock(p, Blocks.AIR.defaultBlockState(), 7));
    }

    public static void populateStructure(ServerLevel level, ResourceLocation template, AABB roomInnerBounds, RoomStructureInfo.RoomStructurePlacement placement) {
        level.getStructureManager().get(template).ifPresent(tem -> {

            Vector3d templateSize = VectorUtils.convert3d(tem.getSize());

            if (!AABBHelper.fitsInside(roomInnerBounds, templateSize)) {
                // skip: structure too large to place in room
                return;
            }

            var placementSettings = new StructurePlaceSettings()
                    .setRotation(Rotation.NONE)
                    .setMirror(Mirror.NONE);

            AABB placementBounds = null;
            final AABBAligner.Bounded aligner = AABBAligner.create(roomInnerBounds, AABBHelper.zeroOriginSized(templateSize));
            switch (placement) {
                case CENTERED -> placementBounds = aligner
                        .center(roomInnerBounds.getCenter())
                        .align();

                case CENTERED_CEILING -> placementBounds = aligner
                        .boundedDirection(Direction.UP)
                        .align();

                case CENTERED_FLOOR -> placementBounds = aligner
                        .boundedDirection(Direction.DOWN)
                        .align();
            }

            if(placementBounds != null) {
                final var pos = AABBHelper.minCorner(placementBounds);
                BlockPos placeAt = BlockPos.containing(pos.x(), pos.y(), pos.z());
                tem.placeInWorld(level, placeAt, placeAt, placementSettings, level.random, Block.UPDATE_ALL);
            }
        });
    }
}
