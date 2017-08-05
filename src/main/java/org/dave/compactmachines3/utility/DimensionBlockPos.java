package org.dave.compactmachines3.utility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.world.tools.DimensionTools;

public class DimensionBlockPos  {
    private BlockPos pos;
    private int dimension;

    public DimensionBlockPos(BlockPos pos, int dimension) {
        this.pos = pos;
        this.dimension = dimension;
    }

    public BlockPos getBlockPos() { return this.pos; }
    public int getDimension() { return this.dimension; }

    public TileEntity getTileEntity() {
        World world = DimensionTools.getWorldServerForDimension(this.dimension);
        return world.getTileEntity(pos);
    }
}
