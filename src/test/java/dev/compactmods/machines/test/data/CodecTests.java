package dev.compactmods.machines.test.data;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

// @GameTestHolder / @ModGameTests here?
public class CodecTests {

    @GameTest(templateNamespace = CompactMachines.MOD_ID, prefixTemplateWithClassname = false, template = "empty_5x")
    public static void testCodec(GameTestHelper test) {
        test.succeed();
    }
}
