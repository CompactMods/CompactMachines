package dev.compactmods.machines.test.worldgen;

import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import dev.compactmods.machines.machine.BuiltInRoomTemplate;
import dev.compactmods.machines.test.TestBatches;
import dev.compactmods.machines.test.util.TestUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class RoomGenerationTests {

    @GameTestGenerator
    public static Collection<TestFunction> roomTests() {
        List<TestFunction> funcs = new ArrayList<>();

        for (var template : BuiltInRoomTemplate.values()) {
            var func = new TestFunction(
                    "room_generation",
                    "builtin_roomgen_" + template.id().getPath(),
                    Constants.MOD_ID + ":empty_15x15",
                    Rotation.NONE,
                    200,
                    0,
                    true,
                    testHelper -> makeTemplateTest(testHelper, template.template())
            );
            funcs.add(func);
        }

        return funcs;
    }

    private static void makeTemplateTest(GameTestHelper testHelper, RoomTemplate template) {
        final AABB localBounds = TestUtil.localBounds(testHelper);
        final BlockPos testCenter = BlockPos.containing(localBounds.getCenter());

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), localBounds);

        testHelper.setBlock(testCenter, Blocks.RED_STAINED_GLASS);
        testHelper.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkOffsetsNormalTest(final GameTestHelper testHelper) {
        final var logs = LogManager.getLogger();

        AABB localBounds = TestUtil.localBounds(testHelper);

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        for(var dir : Direction.values()) {
            var ob = BlockSpaceUtil.centerWallBlockPos(localBounds, dir);
            testHelper.setBlock(ob, Blocks.ORANGE_STAINED_GLASS);
        }

        BlockSpaceUtil.forAllCorners(localBounds).forEach(pos -> {
            testHelper.setBlock(pos, Blocks.BLACK_STAINED_GLASS);
        });

        testHelper.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorNormal(final GameTestHelper testHelper) {

        AABB localBounds = TestUtil.localBounds(testHelper);

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        BlockSpaceUtil.forAllCorners(localBounds.deflate(5))
                .forEach(bp -> testHelper.setBlock(bp, Blocks.IRON_BLOCK));

        testHelper.succeed();
    }

    @GameTest(template = "empty_15x15", batch = TestBatches.ROOM_GENERATION)
    public static void checkRoomGeneratorWeirdShape(final GameTestHelper testHelper) {

        AABB localBounds = TestUtil.localBounds(testHelper);

        final var roomDims = AABB.ofSize(localBounds.getCenter(), 5, 5, 9)
                .move(testHelper.absolutePos(BlockPos.ZERO));

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims);

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        testHelper.succeed();
    }
}
