package dev.compactmods.machines.test.gametest.worldgen;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.CompactRoomGenerator;
import dev.compactmods.machines.api.room.RoomStructureInfo;
import dev.compactmods.machines.test.gametest.core.CompactGameTestHelper;
import dev.compactmods.machines.test.gametest.core.EmptyTestSizes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = "structures")
public class StructureGenTests {

   @TestHolder
   @GameTest
   @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
   public static void structurePlacedCenter(final CompactGameTestHelper testHelper) {

	  AABB localBounds = testHelper.localBounds();

	  final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
		  .move(testHelper.absolutePos(BlockPos.ZERO));

	  CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

	  CompactRoomGenerator.populateStructure(testHelper.getLevel(),
		  CompactMachines.modRL("gold_1x1"),
		  roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED);

	  testHelper.succeed();
   }

   @TestHolder
   @GameTest
   @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
   public static void structurePlacedCeiling(final CompactGameTestHelper testHelper) {

	  AABB localBounds = testHelper.localBounds();

	  final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
		  .move(testHelper.absolutePos(BlockPos.ZERO));

	  CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

	  CompactRoomGenerator.populateStructure(testHelper.getLevel(),
		  CompactMachines.modRL("gold_1x1"),
		  roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED_CEILING);

	  testHelper.succeed();
   }

   @TestHolder
   @GameTest
   @EmptyTemplate(EmptyTestSizes.FIFTEEN_CUBED)
   public static void structurePlacedFloor(final CompactGameTestHelper testHelper) {

	  AABB localBounds = testHelper.localBounds();

	  final var roomDims = AABB.ofSize(localBounds.getCenter(), 11, 11, 9)
		  .move(testHelper.absolutePos(BlockPos.ZERO));

	  CompactRoomGenerator.generateRoom(testHelper.getLevel(), roomDims, Blocks.WHITE_STAINED_GLASS.defaultBlockState());

	  CompactRoomGenerator.populateStructure(testHelper.getLevel(),
		  CompactMachines.modRL("gold_1x1"),
		  roomDims.deflate(1), RoomStructureInfo.RoomStructurePlacement.CENTERED_FLOOR);

	  testHelper.succeed();
   }

}
