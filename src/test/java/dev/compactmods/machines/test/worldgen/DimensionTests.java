package dev.compactmods.machines.test.worldgen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class DimensionTests {

    @GameTest(template = "empty_5x5", batch = TestBatches.DIMENSION)
    public static void dimensionRegistered(final GameTestHelper test) {
        var level = test.getLevel();
        var server = level.getServer();

        var compact = server.getLevel(CompactDimension.LEVEL_KEY);

        if (compact == null)
            test.fail("Compact dimension not registered.");

        test.succeed();
    }
}
