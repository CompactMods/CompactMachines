package org.dave.compactmachines3.schema;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class BlockInformation {
    public BlockPos position;
    public Block block;
    public int meta;
    public NBTTagCompound nbt;
    public boolean writePositionData;

    public BlockInformation(BlockPos position, Block block, int meta, NBTTagCompound nbt, boolean writePositionData) {
        this.position = position;
        this.block = block;
        this.meta = meta;
        this.nbt = nbt;
        this.writePositionData = writePositionData;
    }
}
