package org.dave.compactmachines3.world;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderMachines extends WorldBorder {
    @Override
    public boolean contains(AxisAlignedBB bb) {
        return true;
    }

    @Override
    public boolean contains(BlockPos pos) {
        return true;
    }

    @Override
    public boolean contains(ChunkPos range) {
        return true;
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public void setTransition(double newSize) {
    }

    @Override
    public void setTransition(double oldSize, double newSize, long time) {
    }
}
