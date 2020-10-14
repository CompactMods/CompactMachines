package com.robotgryphon.compactmachines.teleportation;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class DimensionalPosition implements INBTSerializable<CompoundNBT> {

    private ResourceLocation dimension;
    private BlockPos position;

    private DimensionalPosition() { }

    public DimensionalPosition(ResourceLocation dim, BlockPos pos) {
        this.dimension = dim;
        this.position = pos;
    }

    public static DimensionalPosition fromNBT(CompoundNBT nbt) {
        DimensionalPosition dp = new DimensionalPosition();
        dp.deserializeNBT(nbt);

        return dp;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("dim", dimension.toString());
        CompoundNBT posNbt = NBTUtil.writeBlockPos(position);
        nbt.put("pos", posNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("dim"))
        {
            ResourceLocation dim = new ResourceLocation(nbt.getString("dim"));
            this.dimension = dim;
        }

        if(nbt.contains("pos")) {
            CompoundNBT bPosNbt = nbt.getCompound("pos");
            BlockPos bPos = NBTUtil.readBlockPos(bPosNbt);
            this.position = bPos;
        }
    }

    public ResourceLocation getDimension() {
        return this.dimension;
    }

    public BlockPos getPosition() {
        return this.position;
    }
}
