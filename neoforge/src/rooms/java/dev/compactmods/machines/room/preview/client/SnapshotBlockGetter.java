package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * A read-only, fully-lit virtual level over a {@link RoomPreviewSnapshot}, in world coordinates (the
 * room's real positions, from the snapshot's {@link RoomPreviewSnapshot#origin() origin}). It's just enough of a {@link BlockAndTintGetter} for
 * {@link net.minecraft.client.renderer.block.ModelBlockRenderer} to bake the room's block models:
 * neighbour states drive face culling, and brightness is pinned to full so the mesh isn't rendered
 * black (the snapshot carries no light data). No block entities: when the client has a level, the
 * mesher bakes from a {@link PreviewLevel} instead, which adds them and borrows this getter's tints. Tints (grass, foliage, water) resolve
 * against the snapshot's captured biomes, blended over the same radius vanilla uses, so the preview
 * matches the real room. Snapshots without biome data fall back to a single biome.
 */
public final class SnapshotBlockGetter implements BlockAndTintGetter {

    private final RoomPreviewSnapshot snapshot;
    private final Map<ResourceKey<Biome>, Biome> biomes;
    private final @Nullable Biome fallbackBiome;
    private final int blendRadius;

    /**
     * @param biomes        the snapshot's biome keys resolved against the client registry
     * @param fallbackBiome used where the snapshot has no (resolvable) biome; {@code null} renders those
     *                      tinted blocks untinted
     * @param blendRadius   the client's biome-blend radius option, as vanilla's tinting uses
     */
    public SnapshotBlockGetter(RoomPreviewSnapshot snapshot, Map<ResourceKey<Biome>, Biome> biomes,
                               @Nullable Biome fallbackBiome, int blendRadius) {
        this.snapshot = snapshot;
        this.biomes = biomes;
        this.fallbackBiome = fallbackBiome;
        this.blendRadius = blendRadius;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return snapshot.blockAtWorld(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return snapshot.sizeY();
    }

    @Override
    public int getMinY() {
        return snapshot.origin().getY();
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return CardinalLighting.DEFAULT;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return LevelLightEngine.EMPTY;
    }

    /** Mirrors {@code ClientLevel#calculateBlockTint}: average the colour over the blend square at {@code pos.y}. */
    @Override
    public int getBlockTint(BlockPos worldPos, ColorResolver color) {
        final BlockPos pos = worldPos.subtract(snapshot.origin());
        final int y = pos.getY();
        if (blendRadius == 0)
            return colorAt(color, pos.getX(), y, pos.getZ());

        int red = 0, green = 0, blue = 0;
        for (int dz = -blendRadius; dz <= blendRadius; dz++) {
            for (int dx = -blendRadius; dx <= blendRadius; dx++) {
                final int c = colorAt(color, pos.getX() + dx, y, pos.getZ() + dz);
                red += ARGB.red(c);
                green += ARGB.green(c);
                blue += ARGB.blue(c);
            }
        }

        final int count = (blendRadius * 2 + 1) * (blendRadius * 2 + 1);
        return ARGB.color(red / count, green / count, blue / count);
    }

    /** Colour from the biome at room-local {@code (x, y, z)}; the resolver gets world x/z, as in vanilla. */
    private int colorAt(ColorResolver color, int x, int y, int z) {
        final var room = snapshot.biomes();
        final ResourceKey<Biome> key = room.biomeAt(x, y, z);
        final Biome biome = key != null ? biomes.getOrDefault(key, fallbackBiome) : fallbackBiome;
        if (biome == null)
            return -1; // white: tinted blocks render untinted

        return color.getColor(biome, room.originX() + x, room.originZ() + z);
    }

    // Full-bright: LevelLightEngine.EMPTY reports 0 everywhere, which would bake a black mesh.
    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int darkening) {
        return 15;
    }
}
