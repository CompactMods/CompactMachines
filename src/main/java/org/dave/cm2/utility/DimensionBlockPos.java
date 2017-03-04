package org.dave.cm2.utility;

import net.minecraft.util.math.BlockPos;

public class DimensionBlockPos  {
    private BlockPos pos;
    private int dimension;

    public DimensionBlockPos(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
    }

    public BlockPos getBlockPos() { return this.pos; }
    public int getDimension() { return this.dimension; }
}
