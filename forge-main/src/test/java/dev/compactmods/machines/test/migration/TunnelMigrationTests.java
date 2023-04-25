package dev.compactmods.machines.test.migration;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(Constants.MOD_ID)
public class TunnelMigrationTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.MIGRATION)
    public static void canReadCM51RoomTunnelsFile(final GameTestHelper test) {

    }
}
