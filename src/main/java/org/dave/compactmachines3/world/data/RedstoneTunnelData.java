package org.dave.compactmachines3.world.data;

import net.minecraft.util.math.BlockPos;

public class RedstoneTunnelData {
    public BlockPos pos;
    public boolean isOutput;

    public RedstoneTunnelData(BlockPos pos, boolean isOutput) {
        this.pos = pos;
        this.isOutput = isOutput;
    }
}
