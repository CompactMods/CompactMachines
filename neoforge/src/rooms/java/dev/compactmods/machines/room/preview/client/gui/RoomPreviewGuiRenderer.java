package dev.compactmods.machines.room.preview.client.gui;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.IndexType;
import dev.compactmods.machines.room.preview.client.RoomPreviewBlockEntitySubmitter;
import dev.compactmods.machines.room.preview.client.RoomPreviewEntitySubmitter;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import dev.compactmods.machines.room.preview.client.RoomPreviewMesh;
import dev.compactmods.machines.room.preview.client.RoomPreviewMeshCache;
import dev.compactmods.machines.room.preview.client.RoomPreviewOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Draws a room's baked {@link RoomPreviewMesh} into a GUI picture-in-picture texture, from an orbit
 * camera (see {@link PreviewCamera}), with fog when the camera is inside a fluid.
 *
 * <p>Vanilla's PiP flow renders through the feature/submit system, which can't draw our pre-baked
 * vertex buffers, so {@link #prepare} is replaced wholesale: it renders the mesh layers with the
 * vanilla {@code *_BLOCK} pipelines into its own colour + depth textures (perspective, reversed-Z
 * like the world) and blits the result. {@code GuiRenderer} restores its own projection afterwards.
 */
public final class RoomPreviewGuiRenderer extends PictureInPictureRenderer<RoomPreviewGuiState> {

    static final float FOV_DEGREES = 50f;

    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_TRANSFORM = new Matrix4f();

    private @Nullable GpuTexture colorTexture;
    private @Nullable GpuTextureView colorView;
    private @Nullable GpuTexture depthTexture;
    private @Nullable GpuTextureView depthView;

    private final Projection projection = new Projection();
    private final SubmitNodeStorage submitNodes = new SubmitNodeStorage();
    private @Nullable PreviewFog fog;

    /** Rooms whose next rendered preview frame should be saved as a screenshot. */
    private static final Set<String> SCREENSHOT_REQUESTS = ConcurrentHashMap.newKeySet();

    /** Saves the next frame rendered for {@code roomCode} as a screenshot (see {@link PreviewScreenshot}). */
    public static void requestScreenshot(String roomCode) {
        SCREENSHOT_REQUESTS.add(roomCode);
    }
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("PIP - room preview");

    @Override
    public void prepare(RoomPreviewGuiState state, GuiRenderState guiRenderState,
                        FeatureRenderDispatcher featureRenderDispatcher, int guiScale) {
        // No snapshot yet: draw nothing — the panel shows its loading text instead
        final RoomPreviewMesh mesh = RoomPreviewMeshCache.get(state.roomCode());
        final var preview = ClientRoomPreviews.get(state.roomCode());
        if (mesh == null || preview == null)
            return;

        final int width = (state.x1() - state.x0()) * guiScale;
        final int height = (state.y1() - state.y0()) * guiScale;
        if (width <= 0 || height <= 0)
            return;

        ensureTextures(width, height);

        // Fog from what the camera is inside; the void clears to the fog colour too (buffer write: before the pass)
        final Vector3f camera = new Vector3f(state.camX(), state.camY(), state.camZ());
        if (fog == null) fog = new PreviewFog();
        final Vector4fc clearColor = fog.update(preview.snapshot(), camera);

        final var device = RenderSystem.getDevice();
        device.createCommandEncoder().clearColorAndDepthTextures(colorTexture, clearColor,
                depthTexture, RenderSystem.DEFAULT_DEPTH_CLEAR_VALUE);

        projection.setupPerspective(0.05f, 1000f, FOV_DEGREES, width, height);
        RenderSystem.setProjectionMatrix(projectionBuffer.getBuffer(projection), ProjectionType.PERSPECTIVE);

        // Resizing the shared index buffer is only allowed outside a render pass — do it here
        final var quadIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        final var indexBuffer = quadIndices.getBuffer(mesh.maxIndexCount());

        final Matrix4f view = PreviewCamera.view(camera, state.yaw(), state.pitch());
        final var transforms = RenderSystem.getDynamicUniforms()
                .writeTransform(view, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_TRANSFORM);

        // Translucent quads back-to-front from the orbit camera (a buffer write, so before the pass)
        final var translucent = mesh.layer(ChunkSectionLayer.TRANSLUCENT);
        if (translucent != null && translucent.sort() != null)
            translucent.sort().sortFor(camera);

        final var mc = Minecraft.getInstance();

        // Entities go through the feature renderers. As in the world, the camera view sits on the
        // model-view stack and poses stay in room-local space, so entity lighting is world-oriented.
        final Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix().set(view);
        mc.gameRenderer.lighting().setupFor(Lighting.Entry.LEVEL);
        final float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        RoomPreviewEntitySubmitter.submit(state.roomCode(), new PoseStack(), submitNodes, new CameraRenderState(), partialTick);

        // Block entity renderers (chests, ...), for the block entities rebuilt with the mesh
        if (mesh.level() != null)
            RoomPreviewBlockEntitySubmitter.submit(mesh.level(), new Vec3(camera.x, camera.y, camera.z),
                    new PoseStack(), submitNodes, new CameraRenderState(), partialTick);

        // Where (and which way) the viewing player will arrive
        final var spawn = ClientRoomPreviews.spawn(state.roomCode());
        if (spawn != null)
            SpawnGhost.submit(spawn, new PoseStack(), submitNodes, partialTick);

        // Other systems' markers (tunnels, ...)
        for (RoomPreviewOverlay overlay : RoomPreviewOverlay.all())
            overlay.submit(state.roomCode(), new PoseStack(), submitNodes, state.hovered(), partialTick);

        try (var features = featureRenderDispatcher.prepareFrame(submitNodes);
             var pass = device.createCommandEncoder().createRenderPass(
                     () -> CompactMachinesCore.dotPrefix("room_preview_gui"),
                     colorView, Optional.empty(), depthView, OptionalDouble.empty())) {

            // Same order as the world: opaque blocks, entities, then translucent blocks over both
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("Fog", fog.buffer());
            drawLayer(pass, mesh, ChunkSectionLayer.SOLID, transforms, indexBuffer, quadIndices.type());
            drawLayer(pass, mesh, ChunkSectionLayer.CUTOUT, transforms, indexBuffer, quadIndices.type());

            FeatureRenderDispatcher.renderAllFeatures(pass, features);

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("Fog", fog.buffer());
            drawLayer(pass, mesh, ChunkSectionLayer.TRANSLUCENT, transforms, indexBuffer, quadIndices.type());
        } finally {
            modelViewStack.popMatrix();
        }

        if (SCREENSHOT_REQUESTS.remove(state.roomCode()))
            PreviewScreenshot.capture(colorTexture, width, height, state.roomCode());

        blitTexture(state, guiRenderState);
    }

    private static void drawLayer(RenderPass pass, RoomPreviewMesh mesh, ChunkSectionLayer layer, GpuBufferSlice transforms,
                                  GpuBuffer quadIndices, IndexType quadIndexType) {
        final RoomPreviewMesh.LayerMesh lm = mesh.layer(layer);
        if (lm == null)
            return;

        final var mc = Minecraft.getInstance();
        pass.setPipeline(RenderSystem.getCompiledPipeline(RoomPreviewMesh.pipeline(layer)));
        pass.setUniform("Sampler0", mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).getTextureView(),
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
        pass.setUniform("Sampler2", mc.gameRenderer.lightmap(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
        pass.setUniform("DynamicTransforms", transforms);
        pass.setVertexBuffer(0, lm.vertexBuffer().slice());
        if (lm.sort() != null)
            pass.setIndexBuffer(lm.sort().indexBuffer(), lm.sort().indexType());
        else
            pass.setIndexBuffer(quadIndices, quadIndexType);
        pass.drawIndexed(lm.indexCount(), 1, 0, 0, 0);
    }

    /** Camera distance at which the room's bounding sphere just fits the field of view. */
    public static float framingDistance(int sizeX, int sizeY, int sizeZ) {
        final float radius = 0.5f * Mth.sqrt(sizeX * sizeX + sizeY * sizeY + sizeZ * sizeZ);
        return radius / Mth.sin(FOV_DEGREES * Mth.DEG_TO_RAD / 2f);
    }

    private void ensureTextures(int width, int height) {
        if (colorTexture != null && colorTexture.getWidth(0) == width && colorTexture.getHeight(0) == height)
            return;

        closeTextures();

        final var device = RenderSystem.getDevice();
        // Usage flags + depth format as vanilla's PictureInPictureRenderer uses them
        colorTexture = device.createTexture(() -> "UI room preview texture", 13, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
        colorView = device.createTextureView(colorTexture);

        final GpuFormat depthFormat = Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTexture().getFormat();
        depthTexture = device.createTexture(() -> "UI room preview depth texture", 9, depthFormat, width, height, 1, 1);
        depthView = device.createTextureView(depthTexture);
    }

    @Override
    protected void blitTexture(RoomPreviewGuiState state, GuiRenderState guiRenderState) {
        guiRenderState.addBlitToCurrentLayer(new BlitRenderState(
                RenderPipelines.GUI_TEXTURED_PREMULTIPLIED_ALPHA,
                TextureSetup.singleTexture(colorView, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST)),
                state.pose(),
                state.x0(), state.y0(), state.x1(), state.y1(),
                0.0F, 1.0F, 1.0F, 0.0F,
                -1,
                state.scissorArea(),
                null));
    }

    @Override
    protected void renderToTexture(RoomPreviewGuiState state, PoseStack poseStack, SubmitNodeCollector collector) {
        // Unused: prepare() draws the mesh directly
    }

    @Override
    public boolean canBeReusedFor(RoomPreviewGuiState state, int textureWidth, int textureHeight) {
        return colorTexture == null || (colorTexture.getWidth(0) == textureWidth && colorTexture.getHeight(0) == textureHeight);
    }

    @Override
    public Class<RoomPreviewGuiState> getRenderStateClass() {
        return RoomPreviewGuiState.class;
    }

    @Override
    protected String getTextureLabel() {
        return "room preview";
    }

    private void closeTextures() {
        if (colorView != null) colorView.close();
        if (colorTexture != null) colorTexture.close();
        if (depthView != null) depthView.close();
        if (depthTexture != null) depthTexture.close();
        colorView = null;
        colorTexture = null;
        depthView = null;
        depthTexture = null;
    }

    @Override
    public void close() {
        closeTextures();
        projectionBuffer.close();
        if (fog != null) fog.close();
        super.close();
    }
}
