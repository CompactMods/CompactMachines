package dev.compactmods.machines.test;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class MathUtilTests {

    private static final String BATCH = "MathUtil";

    @GameTest(template = "empty_1x1", batch = BATCH)
    public static void positionGeneratorWorksCorrectly(final GameTestHelper test) {
        // Our generation works in a counter-clockwise spiral, starting at 0,0
        /*
         *    6  5  4
         *    7  0  3
         *    8  1  2
         */

        HashMap<Integer, ChunkPos> tests = new HashMap<>();
        tests.put(0, new ChunkPos(0, 0));
        tests.put(1, new ChunkPos(0, -64));
        tests.put(2, new ChunkPos(64, -64));
        tests.put(3, new ChunkPos(64, 0));
        tests.put(4, new ChunkPos(64, 64));
        tests.put(5, new ChunkPos(0, 64));
        tests.put(6, new ChunkPos(-64, 64));
        tests.put(7, new ChunkPos(-64, 0));
        tests.put(8, new ChunkPos(-64, -64));

        tests.forEach((id, expectedChunk) -> {
            Vec3i byIndex = MathUtil.getRegionPositionByIndex(id);
            Vec3 finalPos = MathUtil.getCenterWithY(byIndex, 0);

            ChunkPos calculatedChunk = new ChunkPos(BlockPos.containing(finalPos));

            String error = String.format("Generation did not match for %s.", id);
            if(!expectedChunk.equals(calculatedChunk))
                test.fail(error);
        });

        test.succeed();
    }
}
