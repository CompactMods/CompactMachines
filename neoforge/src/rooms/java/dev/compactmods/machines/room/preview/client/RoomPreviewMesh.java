package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A baked, textured preview of a room: the room's real block models tessellated (via
 * {@link ModelBlockRenderer}) into GPU vertex buffers, one per {@link ChunkSectionLayer}
 * (SOLID/CUTOUT/TRANSLUCENT), in <em>room-local</em> space (unit cubes at {@code (x,y,z)}). The
 * per-machine placement transform is applied at draw time by {@link dev.compactmods.machines.preview.client.RoomPreviewRenderer}, so one
 * baked mesh serves every machine bound to that room.
 *
 * <p>Baking runs on the render thread (it touches the model manager and GPU) and is cached per room
 * version by {@link RoomPreviewMeshCache}; {@link #close()} frees the GPU buffers when a newer
 * snapshot supersedes it.
 */
public final class RoomPreviewMesh {

    public final int sizeX, sizeY, sizeZ;
    private final Map<ChunkSectionLayer, LayerMesh> layers;
    private final @Nullable PreviewLevel level;

    /**
     * One layer's uploaded geometry: a vertex buffer plus its index count. Opaque layers draw with the
     * shared sequential quad indices; the translucent layer carries its own camera-sorted indices.
     */
    public record LayerMesh(GpuBuffer vertexBuffer, int indexCount, @Nullable TranslucentSort sort) {}

    private RoomPreviewMesh(int sizeX, int sizeY, int sizeZ, Map<ChunkSectionLayer, LayerMesh> layers,
                            @Nullable PreviewLevel level) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.layers = layers;
        this.level = level;
    }

    /** {@return the room's rebuilt block entities}, for block entity renderers; null without a client level */
    public @Nullable PreviewLevel level() {
        return level;
    }

    public int maxDimension() {
        return Math.max(sizeX, Math.max(sizeY, sizeZ));
    }

    /** {@return true if there's nothing to draw}: no geometry and no block entities for their renderers */
    public boolean isEmpty() {
        return layers.isEmpty() && (level == null || level.blockEntities().isEmpty());
    }

    /** Draw order for the layers: opaque, then cutout, then translucent last. */
    public static final ChunkSectionLayer[] DRAW_ORDER =
            {ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT, ChunkSectionLayer.TRANSLUCENT};

    /** The vanilla {@code *_BLOCK} pipeline for drawing a layer of this mesh (block atlas + lightmap). */
    public static RenderPipeline pipeline(ChunkSectionLayer layer) {
        return switch (layer) {
            case SOLID -> RenderPipelines.SOLID_BLOCK;
            case CUTOUT -> RenderPipelines.CUTOUT_BLOCK;
            case TRANSLUCENT -> RenderPipelines.TRANSLUCENT_BLOCK;
        };
    }

    /** {@return the largest index count of any layer} — what the shared quad index buffer must hold. */
    public int maxIndexCount() {
        int max = 0;
        for (LayerMesh mesh : layers.values())
            max = Math.max(max, mesh.indexCount());
        return max;
    }

    public @Nullable LayerMesh layer(ChunkSectionLayer layer) {
        return layers.get(layer);
    }

    /** Frees the GPU buffers. Call when this mesh is evicted/superseded. */
    public void close() {
        for (LayerMesh mesh : layers.values()) {
            mesh.vertexBuffer().close();
            if (mesh.sort() != null) mesh.sort().close();
        }
        layers.clear();
    }

    /**
     * Bakes {@code snapshot}'s block models into per-layer GPU buffers. Render thread only.
     *
     * @param latest   block entity data newer than the snapshot's, by room-local position
     * @param previous the room's block entities from its last mesh, updated in place when possible so
     *                 they keep their client state (animation); null to start fresh
     */
    public static RoomPreviewMesh build(RoomPreviewSnapshot snapshot, Map<BlockPos, RoomBlockEntitySnapshot> latest,
                                        @Nullable PreviewLevel previous) {
        final int sx = snapshot.sizeX(), sy = snapshot.sizeY(), sz = snapshot.sizeZ();

        final var mc = Minecraft.getInstance();
        final var modelSet = mc.getModelManager().getBlockStateModelSet();
        final var tints = new SnapshotBlockGetter(snapshot, resolveBiomes(mc, snapshot), compactBiome(mc),
                mc.options.biomeBlendRadius().get());
        // With block entities when possible, so models that read their data (pipe connections) bake right
        final PreviewLevel blockEntities;
        if (mc.level == null)
            blockEntities = null;
        else if (previous != null && previous.canUpdate(mc.level, snapshot))
            blockEntities = previous.update(snapshot, latest, tints);
        else
            blockEntities = PreviewLevel.create(snapshot, latest, mc.level, tints);
        final BlockAndTintGetter level = blockEntities != null ? blockEntities : tints;
        final var renderer = new ModelBlockRenderer(true /*AO*/, true /*cull internal faces*/, mc.getBlockColors());
        final var fluidRenderer = new FluidRenderer(mc.getModelManager().getFluidStateModelSet());
        final var fluidConsumer = new SectionOffsetVertexConsumer();

        final Map<ChunkSectionLayer, ByteBufferBuilder> scratch = new EnumMap<>(ChunkSectionLayer.class);
        final Map<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);

        // Route each baked quad to its own layer's buffer (a block may emit into several layers).
        final BlockQuadOutput out = (x, y, z, quad, instance) ->
                layerBuilder(scratch, builders, quad.materialInfo().layer()).putBlockBakedQuad(x, y, z, quad, instance);
        final BlockQuadOutput forcedSolid = (x, y, z, quad, instance) ->
                layerBuilder(scratch, builders, ChunkSectionLayer.SOLID).putBlockBakedQuad(x, y, z, quad, instance);

        // Levels are queried at world positions (block entities' data can name other blocks by them);
        // vertices are written at room-local ones
        final BlockPos origin = snapshot.origin();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++) {
                    final BlockState state = snapshot.blockAt(x, y, z);
                    if (state.isAir()) continue;
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);

                    // Fluids (water, lava, waterlogged blocks) have no block model; vanilla bakes them separately.
                    final var fluidState = state.getFluidState();
                    if (!fluidState.isEmpty()) {
                        final int fx = x, fy = y, fz = z;
                        final BlockPos world = pos.immutable();
                        fluidRenderer.tesselate(level, pos,
                                layer -> fluidConsumer.target(layerBuilder(scratch, builders, layer), fx, fy, fz, world),
                                state, fluidState);
                    }

                    if (state.getRenderShape() != RenderShape.MODEL) continue;
                    renderer.tesselateBlock(
                            ModelBlockRenderer.forceOpaque(false, state) ? forcedSolid : out,
                            x, y, z, level, pos, state, modelSet.get(state), state.getSeed(pos));
                }
            }
        }

        final Map<ChunkSectionLayer, LayerMesh> result = new EnumMap<>(ChunkSectionLayer.class);
        for (var entry : builders.entrySet()) {
            final MeshData mesh = entry.getValue().build();
            if (mesh == null) continue;
            // Resources close in reverse: the mesh (and its sorted index result) before the scratch it lives in
            try (var indexScratch = new ByteBufferBuilder(mesh.drawState().indexCount() * 4); mesh) {
                final GpuBuffer vb = RenderSystem.getDevice().createBuffer(
                        () -> CompactMachinesCore.dotPrefix("room_preview_verts"),
                        GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());

                // Translucent quads need back-to-front order per camera; start from the room's centre
                TranslucentSort sort = null;
                if (entry.getKey() == ChunkSectionLayer.TRANSLUCENT) {
                    final var state = mesh.sortQuads(indexScratch, VertexSorting.byDistance(sx / 2f, sy / 2f, sz / 2f));
                    if (state != null)
                        sort = new TranslucentSort(state, mesh.indexBuffer(), mesh.drawState().indexCount());
                }

                result.put(entry.getKey(), new LayerMesh(vb, mesh.drawState().indexCount(), sort));
            }
        }
        for (ByteBufferBuilder b : scratch.values())
            b.close();

        return new RoomPreviewMesh(sx, sy, sz, result, blockEntities);
    }

    /** {@return the snapshot's biome keys resolved against the client's synced biome registry} */
    private static Map<ResourceKey<Biome>, Biome> resolveBiomes(Minecraft mc, RoomPreviewSnapshot snapshot) {
        final Map<ResourceKey<Biome>, Biome> resolved = new HashMap<>();
        if (mc.level == null)
            return resolved;

        final var registry = mc.level.registryAccess().lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> key : snapshot.biomes().palette())
            registry.get(key).ifPresent(holder -> resolved.put(key, holder.value()));
        return resolved;
    }

    /** {@return the compact dimension's biome from the client's synced registry, or null if unavailable} */
    private static @Nullable Biome compactBiome(Minecraft mc) {
        if (mc.level == null)
            return null;

        return mc.level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .get(CompactDimension.COMPACT_BIOME_KEY)
                .map(Holder::value)
                .orElse(null);
    }

    private static BufferBuilder layerBuilder(Map<ChunkSectionLayer, ByteBufferBuilder> scratch,
                                              Map<ChunkSectionLayer, BufferBuilder> builders, ChunkSectionLayer layer) {
        return builders.computeIfAbsent(layer, l -> {
            final var bb = new ByteBufferBuilder(l.bufferSize());
            scratch.put(l, bb);
            return new BufferBuilder(bb, PrimitiveTopology.QUADS, l.vertexFormat());
        });
    }
}
