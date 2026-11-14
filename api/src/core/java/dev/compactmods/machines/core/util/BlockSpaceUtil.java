package dev.compactmods.machines.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public class BlockSpaceUtil {

    public static Stream<BlockPos> blocksInside(AABB bounds) {
        return BlockPos.betweenClosedStream(bounds.contract(1, 1, 1));
    }

    public static AABB getWallBounds(AABB area, Direction wall) {
        return getWallBounds(area, wall, 1d);
    }

    public static BlockPos centerWallBlockPos(AABB area, Direction direction) {
        var center = area.getCenter();
        var offset = direction.getAxis().choose(area.getXsize(), area.getYsize(), area.getZsize())
                / 2d;

        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            offset -= 1;

        var centerWallPos = center.relative(direction, offset);
        return BlockPos.containing(centerWallPos);
    }

    public static AABB getPlaneAABB(Direction direction) {
        if (direction.getAxis().isHorizontal())
            return getPlaneAABB(Direction.UP, direction.getCounterClockWise());
        else
            return getPlaneAABB(direction == Direction.UP ? Direction.SOUTH : Direction.NORTH,
                    direction.getClockWise(Direction.Axis.Z));
    }

    public static AABB getPlaneAABB(Direction up, Direction right) {
        Vec3i rightNorm = right.getUnitVec3i();
        Vec3i upNorm = up.getUnitVec3i();

        return AABB.ofSize(Vec3.ZERO, rightNorm.getX() + upNorm.getX(),
                rightNorm.getY() + upNorm.getY(),
                rightNorm.getZ() + upNorm.getZ());
    }

    public static AABB getWallBounds(AABB area, Direction wallDirection, double thickness) {
        var wallCenterDistance = centerWallBlockPos(area, wallDirection);
        Vec3 wallCenter = Vec3.atCenterOf(wallCenterDistance);

        var normal = getPlaneAABB(wallDirection);
        return AABB.ofSize(wallCenter,
                area.getXsize() * normal.getXsize() + (wallDirection.getStepX() * thickness),
                area.getYsize() * normal.getYsize() + (wallDirection.getStepY() * thickness),
                area.getZsize() * normal.getZsize() + (wallDirection.getStepZ() * thickness));
    }
}
