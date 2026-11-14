package dev.compactmods.machines.room.preview;

import io.netty.buffer.Unpooled;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomPreviewBiomesTest {

    private static final List<ResourceKey<Biome>> PALETTE = List.of(Biomes.JUNGLE, Biomes.DESERT, Biomes.OCEAN);

    /** Builds biome data where each quart's biome is picked by {@code (worldQuartX + worldQuartZ) % 3}. */
    private static RoomPreviewBiomes checkerboard(int originX, int originY, int originZ, int sizeX, int sizeY, int sizeZ) {
        final int qMinX = originX >> 2, qMinY = originY >> 2, qMinZ = originZ >> 2;
        final int quartsX = ((originX + sizeX - 1) >> 2) - qMinX + 1;
        final int quartsY = ((originY + sizeY - 1) >> 2) - qMinY + 1;
        final int quartsZ = ((originZ + sizeZ - 1) >> 2) - qMinZ + 1;

        final int[] indices = new int[quartsX * quartsY * quartsZ];
        for (int qy = 0; qy < quartsY; qy++)
            for (int qz = 0; qz < quartsZ; qz++)
                for (int qx = 0; qx < quartsX; qx++)
                    indices[RoomPreviewSnapshot.cellIndex(qx, qy, qz, quartsX, quartsZ)] =
                            Math.floorMod((qMinX + qx) + (qMinZ + qz), 3);

        return new RoomPreviewBiomes(originX, originY, originZ, quartsX, quartsY, quartsZ, PALETTE, indices);
    }

    private static ResourceKey<Biome> expected(int worldX, int worldZ) {
        return PALETTE.get(Math.floorMod((worldX >> 2) + (worldZ >> 2), 3));
    }

    @Test
    void biomeAtMatchesWorldQuartsForUnalignedOrigin() {
        // Room 102V-K3G3-TN39's interior: origin 1022 is not quart-aligned (1022 & 3 == 2)
        final var biomes = checkerboard(1022, 1, 1022, 21, 11, 21);
        for (int x = 0; x < 21; x++)
            for (int z = 0; z < 21; z++)
                assertEquals(expected(1022 + x, 1022 + z), biomes.biomeAt(x, 5, z), "local " + x + "," + z);
    }

    @Test
    void biomeAtMatchesWorldQuartsForNegativeOrigin() {
        final var biomes = checkerboard(-7, -3, -13, 9, 6, 10);
        for (int x = 0; x < 9; x++)
            for (int z = 0; z < 10; z++)
                assertEquals(expected(-7 + x, -13 + z), biomes.biomeAt(x, 0, z), "local " + x + "," + z);
    }

    @Test
    void outOfRoomCoordinatesClampToEdgeQuarts() {
        final var biomes = checkerboard(1022, 1, 1022, 21, 11, 21);
        assertEquals(biomes.biomeAt(0, 0, 0), biomes.biomeAt(-2, -1, -2));
        assertEquals(biomes.biomeAt(20, 10, 20), biomes.biomeAt(23, 12, 23));
    }

    @Test
    void emptyHasNoBiomes() {
        assertNull(RoomPreviewBiomes.EMPTY.biomeAt(0, 0, 0));
    }

    @Test
    void streamCodecRoundTrips() {
        final var original = checkerboard(-7, -3, -13, 9, 6, 10);
        final var buf = Unpooled.buffer();
        RoomPreviewBiomes.STREAM_CODEC.encode(buf, original);
        final var decoded = RoomPreviewBiomes.STREAM_CODEC.decode(buf);

        assertEquals(original, decoded);
        assertEquals(original.contentHash(), decoded.contentHash());
        assertEquals(0, buf.readableBytes(), "decoder should consume everything the encoder wrote");
    }
}
