package dev.compactmods.machines.test.worldgen;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.RoomStructureInfo;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.test.util.TestUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class StructureGenTests {

    private static final String BATCH = "structures";

    @GameTest(template = "empty_15x15", batch = BATCH)
    public static void structurePlacedCenter(final GameTestHelper testHelper) {

        AABB localBounds = TestUtil.localBounds(testHelper);

        final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
                .move(testHelper.absolutePos(BlockPos.ZERO));

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

        CompactRoomGenerator.populateStructure(testHelper.getLevel(),
                CompactMachines.rl("gold_1x1"),
                roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED);

        testHelper.succeed();
    }

    @GameTest(template = "empty_15x15", batch = BATCH)
    public static void structurePlacedCeiling(final GameTestHelper testHelper) {

        AABB localBounds = TestUtil.localBounds(testHelper);

        final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
                .move(testHelper.absolutePos(BlockPos.ZERO));

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

        CompactRoomGenerator.populateStructure(testHelper.getLevel(),
                CompactMachines.rl("gold_1x1"),
                roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED_CEILING);

        testHelper.succeed();
    }

    @GameTest(template = "empty_15x15", batch = BATCH)
    public static void structurePlacedFloor(final GameTestHelper testHelper) {

        AABB localBounds = TestUtil.localBounds(testHelper);

        final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
                .move(testHelper.absolutePos(BlockPos.ZERO));

        CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

        CompactRoomGenerator.populateStructure(testHelper.getLevel(),
                CompactMachines.rl("gold_1x1"),
                roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED_FLOOR);

        testHelper.succeed();
    }

}
