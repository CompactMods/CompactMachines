package dev.compactmods.machines.test.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.test.TestBatches;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(CompactMachines.MOD_ID)
public class CompactMachinesTests {

    @GameTest(template = "empty_1x1", batch = TestBatches.CODEC_TESTS)
    public static void canSerializeVector3d(final GameTestHelper test) {
        try {
            var icon = CompactMachines.COMPACT_MACHINES_ITEMS.makeIcon();
            if(icon.isEmpty())
                test.fail("Mod creative tab icon is wrong.");
        }

        catch(Exception e) {
            test.fail("Mod creative tab nulled on icon creation.");
        }

        test.succeed();
    }
}
