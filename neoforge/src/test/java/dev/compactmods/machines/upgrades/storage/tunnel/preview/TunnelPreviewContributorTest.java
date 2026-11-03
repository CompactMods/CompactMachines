package dev.compactmods.machines.upgrades.storage.tunnel.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TunnelPreviewContributorTest {

    // Interior [10, 15) x [40, 43) x [-5, 0)
    private static final BlockPos MIN = new BlockPos(10, 40, -5), MAX = new BlockPos(15, 43, 0);

    private static Optional<Direction> outward(int x, int y, int z) {
        return TunnelPreviewContributor.outward(new BlockPos(x, y, z), MIN, MAX);
    }

    @Test
    void wallsFaceAwayFromTheInterior() {
        assertEquals(Optional.of(Direction.WEST), outward(9, 41, -3));
        assertEquals(Optional.of(Direction.EAST), outward(15, 41, -3));
        assertEquals(Optional.of(Direction.DOWN), outward(12, 39, -3));
        assertEquals(Optional.of(Direction.UP), outward(12, 43, -3));
        assertEquals(Optional.of(Direction.NORTH), outward(12, 41, -6));
        assertEquals(Optional.of(Direction.SOUTH), outward(12, 41, 0));
    }

    @Test
    void insideIsNotAWall() {
        assertEquals(Optional.empty(), outward(12, 41, -3));
        assertEquals(Optional.empty(), outward(14, 42, -1));
    }
}
