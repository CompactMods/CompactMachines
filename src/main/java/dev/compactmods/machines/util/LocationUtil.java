package dev.compactmods.machines.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class LocationUtil {

    public static Vec3 blockPosToVector(BlockPos pos) {
        return new Vec3(
                pos.getX() + 0.5f,
                pos.getY(),
                pos.getZ() + 0.5f
        );
    }

    public static BlockPos vectorToBlockPos(Vec3 position) {
        return new BlockPos(position.x, position.y, position.z);
    }
}
