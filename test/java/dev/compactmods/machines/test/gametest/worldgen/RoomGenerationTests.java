package dev.compactmods.machines.test.gametest.worldgen;

import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import dev.compactmods.machines.test.gametest.core.CompactGameTestHelper;
import dev.compactmods.machines.test.gametest.core.EmptyTestSizes;
import dev.compactmods.spatial.aabb.AABBHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ForEachTest(groups = "room_generation")
public class RoomGenerationTests {

    @TestHolder
    @GameTestGenerator
    public static Collection<TestFunction> roomTests() {
        List<TestFunction> funcs = new ArrayList<>();

        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("3_cubed"), new RoomTemplate(3, CommonColors.WHITE));
        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("5_cubed"), new RoomTemplate(5, CommonColors.WHITE));
        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("7_cubed"), new RoomTemplate(7, CommonColors.WHITE));
        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("9_cubed"), new RoomTemplate(9, CommonColors.WHITE));
        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("11_cubed"), new RoomTemplate(11, CommonColors.WHITE));
        makeAndAddRoomTemplateTest(funcs, CompactMachines.modRL("13_cubed"), new RoomTemplate(13, CommonColors.WHITE));

        return funcs;
    }

    private static void makeAndAddRoomTemplateTest(List<TestFunction> funcs, ResourceLocation id, RoomTemplate template) {
        funcs.add(new TestFunction(
            "room_generation",
            "builtin_roomgen_" + id.getPath(),
            CompactMachines.modRL("empty_15_cubed").toString(),
            Rotation.NONE,
            200,
            0,
            true,
            testHelper -> templateTest(new CompactGameTestHelper(testHelper.testInfo), template)
        ));
    }

    private static void templateTest(CompactGameTestHelper testHelper, RoomTemplate template) {
        final AABB localBounds = testHelper.localBounds();
        final AABB worldBounds = testHelper.getBounds();
        final BlockPos testCenter = BlockPos.containing(localBounds.getCenter());

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), template.getBoundariesCenteredAt(worldBounds.getCenter()));

        testHelper.setBlock(testCenter, Blocks.RED_STAINED_GLASS);
        testHelper.succeed();
    }

    @TestHolder
    @GameTest
    @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
    public static void checkOffsetsNormalTest(final CompactGameTestHelper testHelper) {
        final var logs = LogManager.getLogger();

        AABB localBounds = testHelper.localBounds();

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        for(var dir : Direction.values()) {
            var ob = BlockSpaceUtil.centerWallBlockPos(localBounds, dir);
            testHelper.setBlock(ob, Blocks.ORANGE_STAINED_GLASS);
        }

        AABBHelper.allCorners(localBounds).forEach(pos -> {
            testHelper.setBlock(pos, Blocks.BLACK_STAINED_GLASS);
        });

        testHelper.succeed();
    }

    @TestHolder
    @GameTest
    @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
    public static void checkRoomGeneratorNormal(final CompactGameTestHelper testHelper) {

        AABB localBounds = testHelper.localBounds();

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        AABBHelper.allCorners(localBounds.deflate(5))
                .forEach(bp -> testHelper.setBlock(bp, Blocks.IRON_BLOCK));

        testHelper.succeed();
    }

    @TestHolder
    @GameTest
    @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
    public static void checkRoomGeneratorWeirdShape(final CompactGameTestHelper testHelper) {

        AABB localBounds = testHelper.localBounds();

        final var roomDims = AABB.ofSize(localBounds.getCenter(), 5, 5, 9)
                .move(testHelper.absolutePos(BlockPos.ZERO));

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims);

        var center = BlockPos.containing(localBounds.getCenter());
        testHelper.setBlock(center, Blocks.GOLD_BLOCK.defaultBlockState());

        testHelper.succeed();
    }
}
