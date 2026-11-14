package dev.compactmods.machines.room.preview;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The biomes of a room interior, captured alongside its blocks so the client can tint the preview
 * (grass, foliage, water) the same way the real room is tinted.
 *
 * <p>Biomes are stored at the game's own resolution — one entry per 4×4×4 "quart" cell — over the
 * quarts the interior overlaps, using the same palette + bit-packed index scheme as the block grid.
 * {@link #originX()}/{@link #originY()}/{@link #originZ()} are the world coordinates of the
 * interior's minimum corner (room-local {@code (0,0,0)}); they map local block positions onto world
 * quarts, and give tint resolvers the world x/z they expect.
 */
public record RoomPreviewBiomes(int originX, int originY, int originZ,
                                int quartsX, int quartsY, int quartsZ,
                                List<ResourceKey<Biome>> palette, int[] indices) {

    /** No biome data — {@link #biomeAt} returns {@code null} everywhere. */
    public static final RoomPreviewBiomes EMPTY = new RoomPreviewBiomes(0, 0, 0, 0, 0, 0, List.of(), new int[0]);

    private static final StreamCodec<ByteBuf, List<ResourceKey<Biome>>> PALETTE_STREAM_CODEC =
            ResourceKey.streamCodec(Registries.BIOME).apply(ByteBufCodecs.list());

    public static final StreamCodec<ByteBuf, RoomPreviewBiomes> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RoomPreviewBiomes decode(ByteBuf buf) {
            final int originX = ByteBufCodecs.VAR_INT.decode(buf);
            final int originY = ByteBufCodecs.VAR_INT.decode(buf);
            final int originZ = ByteBufCodecs.VAR_INT.decode(buf);
            final int quartsX = ByteBufCodecs.VAR_INT.decode(buf);
            final int quartsY = ByteBufCodecs.VAR_INT.decode(buf);
            final int quartsZ = ByteBufCodecs.VAR_INT.decode(buf);
            final List<ResourceKey<Biome>> palette = PALETTE_STREAM_CODEC.decode(buf);

            final int cellCount = Math.multiplyExact(Math.multiplyExact(quartsX, quartsY), quartsZ);
            final int[] indices = RoomPreviewSnapshot.unpack(buf, cellCount, RoomPreviewSnapshot.bitsFor(palette.size()));
            return new RoomPreviewBiomes(originX, originY, originZ, quartsX, quartsY, quartsZ, palette, indices);
        }

        @Override
        public void encode(ByteBuf buf, RoomPreviewBiomes biomes) {
            ByteBufCodecs.VAR_INT.encode(buf, biomes.originX);
            ByteBufCodecs.VAR_INT.encode(buf, biomes.originY);
            ByteBufCodecs.VAR_INT.encode(buf, biomes.originZ);
            ByteBufCodecs.VAR_INT.encode(buf, biomes.quartsX);
            ByteBufCodecs.VAR_INT.encode(buf, biomes.quartsY);
            ByteBufCodecs.VAR_INT.encode(buf, biomes.quartsZ);
            PALETTE_STREAM_CODEC.encode(buf, biomes.palette);
            RoomPreviewSnapshot.pack(buf, biomes.indices, RoomPreviewSnapshot.bitsFor(biomes.palette.size()));
        }
    };

    /**
     * Reads the noise biomes of every quart overlapping the block box starting at
     * {@code (minX, minY, minZ)} of the given size. Server thread only.
     */
    public static RoomPreviewBiomes capture(ServerLevel level, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
        final int qMinX = minX >> 2, qMinY = minY >> 2, qMinZ = minZ >> 2;
        final int quartsX = ((minX + sizeX - 1) >> 2) - qMinX + 1;
        final int quartsY = ((minY + sizeY - 1) >> 2) - qMinY + 1;
        final int quartsZ = ((minZ + sizeZ - 1) >> 2) - qMinZ + 1;

        final List<ResourceKey<Biome>> palette = new ArrayList<>();
        final Object2IntMap<ResourceKey<Biome>> lookup = new Object2IntOpenHashMap<>();
        lookup.defaultReturnValue(-1);

        final int[] indices = new int[quartsX * quartsY * quartsZ];
        for (int qy = 0; qy < quartsY; qy++) {
            for (int qz = 0; qz < quartsZ; qz++) {
                for (int qx = 0; qx < quartsX; qx++) {
                    final ResourceKey<Biome> key = level.getNoiseBiome(qMinX + qx, qMinY + qy, qMinZ + qz)
                            .unwrapKey()
                            .orElse(Biomes.PLAINS);

                    int id = lookup.getInt(key);
                    if (id == -1) {
                        id = palette.size();
                        palette.add(key);
                        lookup.put(key, id);
                    }
                    indices[RoomPreviewSnapshot.cellIndex(qx, qy, qz, quartsX, quartsZ)] = id;
                }
            }
        }

        return new RoomPreviewBiomes(minX, minY, minZ, quartsX, quartsY, quartsZ, palette, indices);
    }

    /**
     * {@return the biome at room-local block coordinates, or {@code null} if no biome data was captured}
     * Coordinates outside the interior clamp to the nearest edge quart, so tint blending near the walls
     * samples the room's own biomes rather than falling back to a default.
     */
    public @Nullable ResourceKey<Biome> biomeAt(int x, int y, int z) {
        if (palette.isEmpty())
            return null;

        final int qx = Mth.clamp(((originX + x) >> 2) - (originX >> 2), 0, quartsX - 1);
        final int qy = Mth.clamp(((originY + y) >> 2) - (originY >> 2), 0, quartsY - 1);
        final int qz = Mth.clamp(((originZ + z) >> 2) - (originZ >> 2), 0, quartsZ - 1);
        return palette.get(indices[RoomPreviewSnapshot.cellIndex(qx, qy, qz, quartsX, quartsZ)]);
    }

    public int contentHash() {
        int h = originX;
        h = 31 * h + originY;
        h = 31 * h + originZ;
        h = 31 * h + quartsX;
        h = 31 * h + quartsY;
        h = 31 * h + quartsZ;
        h = 31 * h + palette.hashCode();
        h = 31 * h + Arrays.hashCode(indices);
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomPreviewBiomes other)) return false;
        return originX == other.originX && originY == other.originY && originZ == other.originZ
                && quartsX == other.quartsX && quartsY == other.quartsY && quartsZ == other.quartsZ
                && palette.equals(other.palette) && Arrays.equals(indices, other.indices);
    }

    @Override
    public int hashCode() {
        return contentHash();
    }

    @Override
    public String toString() {
        return "RoomPreviewBiomes[" + quartsX + "x" + quartsY + "x" + quartsZ + " quarts, palette=" + palette + "]";
    }
}
