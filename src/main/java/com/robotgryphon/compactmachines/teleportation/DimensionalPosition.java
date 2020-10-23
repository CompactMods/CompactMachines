package com.robotgryphon.compactmachines.teleportation;

import com.robotgryphon.compactmachines.data.NbtDataUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.INBTSerializable;

public class DimensionalPosition implements INBTSerializable<CompoundNBT> {

    private ResourceLocation dimension;
    private Vector3d position;

    private DimensionalPosition() { }

    public DimensionalPosition(ResourceLocation dim, Vector3d pos) {
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
        CompoundNBT posNbt = NbtDataUtil.writeVectorCompound(position);
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
            Vector3d bPos = NbtDataUtil.readVectorCompound(bPosNbt);
            this.position = bPos;
        }
    }

    public ResourceLocation getDimension() {
        return this.dimension;
    }

    public Vector3d getPosition() {
        return this.position;
    }
}
