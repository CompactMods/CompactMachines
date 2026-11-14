package dev.compactmods.machines.preview.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.room.preview.client.RoomPreviewEntities;
import dev.compactmods.machines.room.preview.client.RoomPreviewEntitySubmitter;
import dev.compactmods.machines.room.preview.client.RoomPreviewMesh;
import dev.compactmods.machines.room.preview.client.RoomPreviewMeshCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.PrepareRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws the textured room previews. Each room is baked once into per-layer GPU buffers
 * ({@link RoomPreviewMesh}, in room-local space); this renderer draws that mesh once per visible
 * machine, applying the machine's placement transform via the per-draw model-view matrix.
 *
 * <p>Draws straight into the level's main pass — opaque layers at
 * {@link RenderLevelStageEvent.AfterOpaqueBlocks}, the translucent layer after the level's solid
 * features (so the room's entities show through its water) — since NeoForge 26.3 stages hand out the
 * active {@code RenderPass} and opening another one mid-frame is illegal. That pass already targets the main colour + depth, so the vanilla {@code *_BLOCK}
 * pipelines (reversed-Z depth test+write, block atlas + lightmap) occlude correctly against the world,
 * and the machines' translucent panes composite over the preview later in the frame. The shared quad
 * index buffer can't be resized inside the pass, so its size is requested in
 * {@link #prepareBuffers(PrepareRenderBuffersEvent)}.
 */
public final class RoomPreviewRenderer {

    /** How much of the block the preview fills (the machine's inner pane window). */
    private static final float FIT = 14f / 16f;
    /** Block-space Y of the inner window's floor — the room's floor is anchored here. */
    private static final float FLOOR_Y = (1f - FIT) / 2f;

    private static final ContextKey<State> KEY = new ContextKey<>(CompactMachinesCore.identifier("room_preview_renderer"));

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_TRANSFORM = new Matrix4f();
    private RoomPreviewRenderer() {}

    // --- extraction: collect visible machines + ensure their room meshes are baked ---

    public static void extract(ExtractLevelRenderStateEvent event) {
        final State state = event.getRenderState().getRenderDataOrDefault(KEY, State.INSTANCE);
        state.entries.clear();

        final var frustum = event.getFrustum();
        state.camX = frustum.getCamX();
        state.camY = frustum.getCamY();
        state.camZ = frustum.getCamZ();

        event.getLevelExtractor().iterateVisibleBlockEntities(be -> {
            if (!(be instanceof CompactMachineBlockEntity machine)) return;
            final String code = machine.connectedRoom().orElse(null);
            if (code == null) return;
            final RoomPreviewMesh mesh = RoomPreviewMeshCache.get(code);
            if (mesh == null) return;
            state.entries.add(new Entry(machine.getBlockPos(), code, mesh));
        });

        event.getRenderState().setRenderData(KEY, state);
    }

    // --- drawing ---------------------------------------------------------------------

    /**
     * Before the main pass opens: reserve the quad index buffer for the largest preview layer, and
     * re-sort translucent layers back-to-front (both are buffer writes, illegal inside the pass).
     */
    public static void prepareBuffers(PrepareRenderBuffersEvent event) {
        final State state = event.getLevelRenderState().getRenderData(KEY);
        if (state == null || state.entries.isEmpty()) return;

        final int maxIndices = state.maxIndexCount();
        if (maxIndices > 0)
            RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS).requestIndexCount(maxIndices);

        // A mesh is shared by every machine bound to its room, but has one index order: sort for the
        // nearest of those machines, the one whose layering is most visible.
        final Map<RoomPreviewMesh, Entry> nearest = new IdentityHashMap<>();
        for (Entry entry : state.entries) {
            nearest.merge(entry.mesh, entry, (a, b) ->
                    distanceSq(a, state) <= distanceSq(b, state) ? a : b);
        }

        nearest.forEach((mesh, entry) -> {
            final var translucent = mesh.layer(ChunkSectionLayer.TRANSLUCENT);
            if (translucent != null && translucent.sort() != null)
                translucent.sort().sortFor(cameraInMesh(entry, state));
        });
    }

    private static double distanceSq(Entry entry, State state) {
        return entry.pos.distToCenterSqr(state.camX, state.camY, state.camZ);
    }

    /** Inverse of {@link #modelView}'s placement: the camera position in the mesh's room-local space. */
    private static Vector3f cameraInMesh(Entry entry, State state) {
        final float scale = FIT / entry.mesh.maxDimension();
        return new Vector3f(
                (float) (state.camX - entry.pos.getX() - 0.5) / scale + entry.mesh.sizeX / 2f,
                (float) (state.camY - entry.pos.getY() - FLOOR_Y) / scale,
                (float) (state.camZ - entry.pos.getZ() - 0.5) / scale + entry.mesh.sizeZ / 2f);
    }

    /** Opaque preview layers: drawn before the level's solid features (entities), like solid terrain. */
    public static void afterSolidBlocks(RenderLevelStageEvent.AfterOpaqueBlocks event) {
        drawLayers(event.getLevelRenderState().getRenderData(KEY), event.getRenderPass(),
                ChunkSectionLayer.SOLID, ChunkSectionLayer.CUTOUT);
    }

    /**
     * The translucent preview layer: drawn after the level's solid features, like translucent terrain.
     * Translucent quads write depth, so drawn earlier the preview's water would hide the room's
     * entities swimming in it. Fires in the main pass with classic and improved transparency alike.
     */
    public static void afterOpaqueFeatures(RenderLevelStageEvent.AfterOpaqueFeatures event) {
        drawLayers(event.getLevelRenderState().getRenderData(KEY), event.getRenderPass(),
                ChunkSectionLayer.TRANSLUCENT);
    }

    private static void drawLayers(@Nullable State state, RenderPass pass, ChunkSectionLayer... layers) {
        if (state == null || state.entries.isEmpty()) return;

        final var quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        // getBuffer() would resize mid-pass if prepareBuffers didn't reserve enough; skip this frame instead
        if (!quadIndices.hasStorage(state.maxIndexCount())) return;

        final Minecraft mc = Minecraft.getInstance();
        final Matrix4f view = RenderSystem.getModelViewMatrixCopy();
        final var atlas = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView();
        final var lightmap = mc.gameRenderer.lightmap();
        final var atlasSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
        final var lightmapSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        pass.pushDebugGroup(() -> CompactMachinesCore.dotPrefix("room_preview"));
        try {
            for (ChunkSectionLayer layer : layers) {
                final RenderPipeline pipeline = RoomPreviewMesh.pipeline(layer);
                pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("Sampler0", atlas, atlasSampler);
                pass.setUniform("Sampler2", lightmap, lightmapSampler);

                for (Entry entry : state.entries) {
                    final RoomPreviewMesh.LayerMesh lm = entry.mesh.layer(layer);
                    if (lm == null) continue;

                    final Matrix4f modelView = modelView(view, entry, state.camX, state.camY, state.camZ);
                    final var transforms = RenderSystem.getDynamicUniforms()
                            .writeTransform(modelView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_TRANSFORM);

                    pass.setUniform("DynamicTransforms", transforms);
                    pass.setVertexBuffer(0, lm.vertexBuffer().slice());
                    if (lm.sort() != null)
                        pass.setIndexBuffer(lm.sort().indexBuffer(), lm.sort().indexType());
                    else
                        pass.setIndexBuffer(quadIndices.getBuffer(lm.indexCount()), quadIndices.type());
                    pass.drawIndexed(lm.indexCount(), 1, 0, 0, 0);
                }
            }
        } finally {
            pass.popDebugGroup();
        }
    }

    /**
     * Injects the live tracked entities into the level's own submit pass, so they are drawn by the
     * regular entity/feature renderers (which can't be invoked re-entrantly from a stage event). Each
     * entity is submitted under its machine's placement transform, matching the block mesh; the level
     * then draws them into the world at the shrunk-down preview location.
     */
    public static void onSubmitCustomGeometry(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
        final State state = event.getLevelRenderState().getRenderData(KEY);
        if (state == null || state.entries.isEmpty()) return;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        RoomPreviewEntities.retainCurrent();

        final float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        final var camera = event.getLevelRenderState().cameraRenderState;
        final var collector = event.getSubmitNodeCollector();
        final PoseStack pose = event.getPoseStack();

        for (Entry entry : state.entries) {
            final float scale = FIT / entry.mesh.maxDimension();

            // Camera-relative machine transform (camera view is applied by the level), then
            // room-local space — matching the block mesh placement.
            pose.pushPose();
            pose.translate(entry.pos.getX() - state.camX, entry.pos.getY() - state.camY, entry.pos.getZ() - state.camZ);
            pose.translate(0.5f, FLOOR_Y, 0.5f);
            pose.scale(scale, scale, scale);
            pose.translate(-entry.mesh.sizeX / 2f, 0f, -entry.mesh.sizeZ / 2f);
            RoomPreviewEntitySubmitter.submit(entry.roomCode, pose, collector, camera, partialTick);
            pose.popPose();
        }
    }

    /** Model-view for one machine: camera view × (block-relative-to-camera) × uniform-fit, floor-anchored. */
    private static Matrix4f modelView(Matrix4f view, Entry entry, double camX, double camY, double camZ) {
        final RoomPreviewMesh mesh = entry.mesh;
        final BlockPos p = entry.pos;
        final float scale = FIT / mesh.maxDimension();

        return new Matrix4f(view)
                .translate((float) (p.getX() - camX), (float) (p.getY() - camY), (float) (p.getZ() - camZ))
                .translate(0.5f, FLOOR_Y, 0.5f)
                .scale(scale)
                .translate(-mesh.sizeX / 2f, 0f, -mesh.sizeZ / 2f);
    }

    private record Entry(BlockPos pos, String roomCode, RoomPreviewMesh mesh) {}

    private static final class State {
        static final State INSTANCE = new State();
        final List<Entry> entries = new ArrayList<>();
        double camX, camY, camZ;

        int maxIndexCount() {
            int max = 0;
            for (Entry entry : entries)
                max = Math.max(max, entry.mesh.maxIndexCount());
            return max;
        }
    }
}
