package dev.compactmods.machines.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class LocationUtil {

    public static Vector3d blockPosToVector(BlockPos pos) {
        return new Vector3d(
                pos.getX() + 0.5f,
                pos.getY(),
                pos.getZ() + 0.5f
        );
    }

    public static BlockPos vectorToBlockPos(Vector3d position) {
        return new BlockPos(position.x, position.y, position.z);
    }
}
