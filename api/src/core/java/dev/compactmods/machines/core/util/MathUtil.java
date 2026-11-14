package dev.compactmods.machines.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public class MathUtil {

    public static Stream<ChunkPos> getChunksFromAABB(AABB bounds) {
        ChunkPos min = ChunkPos.containing(BlockPos.containing(bounds.minX, bounds.minY, bounds.minZ));
        ChunkPos max = ChunkPos.containing(BlockPos.containing(bounds.maxX, bounds.maxY, bounds.maxZ));
        
        return ChunkPos.rangeClosed(min, max);
    }

    public static int volumeOf(Vec3i dimensions) {
        return dimensions.getX() * dimensions.getY() * dimensions.getZ();
    }

    /**
     *
     * @param i
     * @return
     */
    public static Vec3i getRegionPositionByIndex(int i) {
        // From SO, https://stackoverflow.com/a/41141648
        int index = i + 1;
        // wth
        int s = (int) Math.ceil(Math.sqrt(index)) + (int) ((Math.ceil(Math.sqrt(index)) % 2 + 1) % 2);
        int ringIndex = 0;
        int p = 1;
        if (s > 1) {
            ringIndex = i - (s - 2) * (s - 2);
            p = s * s - (s - 2) * (s - 2);
        }

        int ri = (ringIndex + (s / 2)) % p;

        int x = 0;
        if (s > 1) {
            if (ri < (p / 4)) x = ri;
            else {
                if (ri <= (p / 4 * 2 - 1)) x = p / 4;
                else {
                    if (ri <= (p / 4 * 3)) x = (p / 4 * 3) - ri;
                    else x = 0;
                }
            }
        }

        int y = 0;
        if (s > 1)
            y = ri < (p / 4) ? 0 :
                    (ri <= (p / 4 * 2 - 1) ? (ri - (p / 4)) :
                            (ri <= (p / 4 * 3) ? (p / 4) :
                                    (p - ri)));

        x -= s / 2;
        y -= s / 2;

        return new Vec3i(x, 0, y);
    }

    public static Vec3 getCenterWithY(ChunkPos chunk, int y) {
        return Vec3.atBottomCenterOf(chunk.getWorldPosition()
                .offset(new BlockPos(8, y, 8)));
    }

    public static Vec3 getCenterWithY(Vec3i regionIndex, int y) {
        ChunkPos chunk = new ChunkPos(
                regionIndex.getX() * 64,
                regionIndex.getZ() * 64);

        return getCenterWithY(chunk, y);
    }

}
