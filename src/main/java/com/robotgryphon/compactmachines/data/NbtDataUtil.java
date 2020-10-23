package com.robotgryphon.compactmachines.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;

public abstract class NbtDataUtil {

    public static CompoundNBT writeVectorCompound(Vector3d vector) {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putDouble("x", vector.x);
        nbt.putDouble("y", vector.y);
        nbt.putDouble("z", vector.z);
        return nbt;
    }

    public static Vector3d readVectorCompound(CompoundNBT nbt) {
        double x = nbt.getDouble("x");
        double y = nbt.getDouble("y");
        double z = nbt.getDouble("z");
        return new Vector3d(x, y, z);
    }
}
