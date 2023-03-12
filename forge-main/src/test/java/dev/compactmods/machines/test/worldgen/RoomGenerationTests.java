package dev.compactmods.machines.test.worldgen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.forge.wall.Walls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class RoomGenerationTests {

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorColossal(final GameTestHelper test) {
        final var roomDims = new Vec3i(13, 13, 13);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 1, 7)));

        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        CompactStructureGenerator.generateRoom(test.getLevel(), roomDims, roomCenter, unbreakableWall);

        test.setBlock(new BlockPos(7, 8, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactStructureGenerator.fillWithTemplate(test.getLevel(),
                new ResourceLocation(Constants.MOD_ID, "template_max"),
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorNormal(final GameTestHelper test) {
        final var roomDims = new Vec3i(9, 9, 9);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 1, 7)));

        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        CompactStructureGenerator.generateRoom(test.getLevel(), roomDims, roomCenter, unbreakableWall);

        test.setBlock(new BlockPos(7, 5, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactStructureGenerator.fillWithTemplate(test.getLevel(),
                RoomTemplate.NO_TEMPLATE,
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorSmall(final GameTestHelper test) {
        final var roomDims = new Vec3i(5, 5, 5);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 1, 7)));

        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        CompactStructureGenerator.generateRoom(test.getLevel(), roomDims, roomCenter, unbreakableWall);

        test.setBlock(new BlockPos(7, 4, 7), Blocks.GOLD_BLOCK.defaultBlockState());
        CompactStructureGenerator.fillWithTemplate(test.getLevel(),
                RoomTemplate.NO_TEMPLATE,
                roomDims, roomCenter);

        test.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorWeirdShape(final GameTestHelper test) {
        final var roomDims = new Vec3i(11, 2, 7);
        final var roomCenter = Vec3.atCenterOf(test.absolutePos(new BlockPos(7, 1, 7)));

        final var unbreakableWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();
        CompactStructureGenerator.generateRoom(test.getLevel(), roomDims, roomCenter, unbreakableWall);

        // test.setBlock(new BlockPos(7, 3, 7), Blocks.GOLD_BLOCK.defaultBlockState());

        test.succeed();
    }
}
