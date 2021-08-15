package dev.compactmods.machines.level;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.BiomeContainer;

public class BiomeHelper {
    private static final int WIDTH_BITS = (int)Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;

    public static int biomeIndex(BlockPos pos) {
        int l = (pos.getX() >> 2) & BiomeContainer.HORIZONTAL_MASK;
        int m = MathHelper.clamp(pos.getY() >> 2, 0, BiomeContainer.VERTICAL_MASK);
        int n = (pos.getZ() >> 2) & BiomeContainer.HORIZONTAL_MASK;
        return m << WIDTH_BITS + WIDTH_BITS
                | n << WIDTH_BITS
                | l;
    }
}
