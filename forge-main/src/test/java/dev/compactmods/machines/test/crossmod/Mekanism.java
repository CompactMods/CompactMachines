package dev.compactmods.machines.test.crossmod;

import dev.compactmods.machines.forge.CompactMachines;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
public class Mekanism {

    public static final String MEKANISM_BATCH = "mekanism";

    @GameTest(templateNamespace = "compactmachines", template = "empty_1x1", batch = MEKANISM_BATCH)
    public static void testMekanism(final GameTestHelper test) {
        CompactMachines.LOGGER.debug("Testing mekanism interactions!");
        test.succeed();
    }
}
