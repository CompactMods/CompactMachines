package dev.compactmods.machines.test.gametest.core;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.world.phys.AABB;

public final class CompactGameTestHelper extends GameTestHelper {

    public CompactGameTestHelper(GameTestInfo testInfo) {
        super(testInfo);
    }

    public AABB localBounds() {
        var bounds = getBounds();
        return bounds.move(BlockPos.ZERO.subtract(absolutePos(BlockPos.ZERO)));
    }

}
