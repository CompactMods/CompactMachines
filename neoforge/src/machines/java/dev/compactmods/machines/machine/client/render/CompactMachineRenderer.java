package dev.compactmods.machines.machine.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.compactmods.machines.machine.client.shader.MachineShaders;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CompactMachineRenderer implements BlockEntityRenderer<CompactMachineBlockEntity, MachineRenderState> {

    /**
     * Tiny outward offset so the overlay always wins the depth test against the panes.
     */
    private static final float OUTSET = 0.001f;
    /**
     * Pane geometry in block-local space (matches the model — 14×14 inset by 1px).
     */
    private static final float A = 1f / 16f;
    private static final float B = 15f / 16f;

    /** Radians per tick — one full turn every {@code 2π / RPS ≈ 125 ticks} (~6.3 s). */
    private static final float CORE_SPIN_RATE = 0.05f;
    /** Peak vertical displacement of the bob, in block units. */
    private static final float CORE_BOB_AMPLITUDE = 0.08f;
    /** Tick frequency of the bob — slow enough to feel like it's floating. */
    private static final float CORE_BOB_RATE = 0.1f;
    /** Uniform scale applied to the GROUND-context item so it reads as a held core. */
    private static final float CORE_ITEM_SCALE = 1.2f;

    private final ItemModelResolver itemModelResolver;

    public CompactMachineRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(CompactMachineBlockEntity be, MachineRenderState state,
                                   float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        // gameTime stays on the state for future shaders that might want a frame-
        // stamped tick value; the current pride shader reads the Globals UBO directly.
        state.gameTime = be.getLevel() != null ? be.getLevel().getGameTime() + partialTick : 0f;
        extractCoreItem(be, state);
    }

    /**
     * Pull the core item out of the BE's 1-slot handler and bake an
     * {@link net.minecraft.client.renderer.item.ItemStackRenderState} for it.
     * Only template cores (room pending creation) are drawn — once a core is
     * bound to a room, the room preview takes its place. When there's nothing
     * to draw we clear the cached render state so its layers release any
     * references — same pattern as vanilla's lectern / item-frame BERs in the
     * deferred pipeline.
     */
    private void extractCoreItem(CompactMachineBlockEntity be, MachineRenderState state) {
        final var resource = be.coreHandler().getResource(0);
        final boolean pendingRoom = !resource.isEmpty()
                && resource.has(Rooms.DataComponents.ROOM_TEMPLATE_ID)
                && !resource.has(Rooms.DataComponents.BOUND_ROOM_CODE);

        if (!pendingRoom) {
            state.coreItem.clear();
            state.hasCoreItem = false;
            return;
        }

        ItemStack stack = resource.toStack(1);
        // Block-pos hash gives a stable per-block seed for any randomised
        // item-model variants (e.g. tinted layers). Mirrors what
        // BlockEntity-bound renderers use elsewhere in vanilla.
        int seed = be.getBlockPos().hashCode();
        this.itemModelResolver.updateForTopItem(
                state.coreItem, stack, ItemDisplayContext.GROUND,
                be.getLevel(), null, seed);
        state.hasCoreItem = !state.coreItem.isEmpty();
    }

    @Override
    public void submit(MachineRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        submitCoreItem(state, poseStack, collector);
    }

    /**
     * Float the core item at the centre of the block and slowly spin it on
     * the Y axis — same general approach as the enchantment table book.
     * The shader overlay is drawn around it later in {@link #submit}.
     */
    private static void submitCoreItem(MachineRenderState state, PoseStack poseStack,
                                       SubmitNodeCollector collector) {
        if (!state.hasCoreItem) return;

        final float bob = Mth.sin(state.gameTime * CORE_BOB_RATE) * CORE_BOB_AMPLITUDE;
        final float yaw = state.gameTime * CORE_SPIN_RATE;

        poseStack.pushPose();
        // Centre of the block, then bob — the GROUND-context model sits
        // around the origin so this places it dead-centre in the cube.
        poseStack.translate(0.5f, 0.4f, 0.5f);
        poseStack.rotate(Axis.YP, yaw);
        poseStack.scale(CORE_ITEM_SCALE, CORE_ITEM_SCALE, CORE_ITEM_SCALE);

        state.coreItem.submit(poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY, /* outlineColor = */ 0);
        poseStack.popPose();
    }

    // --- geometry --------------------------------------------------------------------

    /**
     * Emit one quad per face, slightly outside each glass pane. Per-vertex
     * {@code Color.rgb} carries the block-local XYZ for the shader (packed as
     * three bytes in [0,255]); {@code Color.a} is full opacity (the blend
     * opacity is set by the fragment shader, not the vertex alpha);
     * {@code Normal} carries the outward face direction.
     */
    public static void emitPanes(PoseStack.Pose pose, VertexConsumer buffer, int skipMask) {
        // Side panes: each at the face's outer plane, 14×14 area between frame bars.
        // Sentinel for the "outside" axis: 0 - OUTSET on the negative face, 1 + OUTSET on the positive.
        if (!isSkipped(skipMask, Direction.NORTH))
            emitFace(pose, buffer, Direction.NORTH, -OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.SOUTH))
            emitFace(pose, buffer, Direction.SOUTH, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.WEST))
            emitFace(pose, buffer, Direction.WEST, -OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.EAST))
            emitFace(pose, buffer, Direction.EAST, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.UP))
            emitFace(pose, buffer, Direction.UP, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.DOWN))
            emitFace(pose, buffer, Direction.DOWN, -OUTSET, A, A, B, B);
    }

    private static boolean isSkipped(int mask, Direction dir) {
        return (mask & (1 << dir.get3DDataValue())) != 0;
    }

    /**
     * Emit one quad for the named face. {@code plane} is the coordinate along the
     * face's normal axis (slightly outside the pane). The {@code u1..v2} are the
     * in-plane extents — they line up with the inner 14×14 window between the
     * frame bars.
     */
    public static void emitFace(PoseStack.Pose pose, VertexConsumer buf,
                                Direction face, float plane,
                                float u1, float v1, float u2, float v2) {
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();

        // For each face we pick the in-plane axes so the quad winds counter-clockwise
        // when viewed from outside the cube (correct facing for backface culling).
        switch (face) {
            case NORTH -> { // outward = -Z, plane is along Z; u=X, v=Y
                vertex(pose, buf, u2, v1, plane, nx, ny, nz);
                vertex(pose, buf, u1, v1, plane, nx, ny, nz);
                vertex(pose, buf, u1, v2, plane, nx, ny, nz);
                vertex(pose, buf, u2, v2, plane, nx, ny, nz);
            }
            case SOUTH -> { // outward = +Z, plane is along Z; u=X, v=Y (reverse wind)
                vertex(pose, buf, u1, v1, plane, nx, ny, nz);
                vertex(pose, buf, u2, v1, plane, nx, ny, nz);
                vertex(pose, buf, u2, v2, plane, nx, ny, nz);
                vertex(pose, buf, u1, v2, plane, nx, ny, nz);
            }
            case WEST -> {  // outward = -X, plane is along X; u=Z, v=Y
                vertex(pose, buf, plane, v1, u1, nx, ny, nz);
                vertex(pose, buf, plane, v1, u2, nx, ny, nz);
                vertex(pose, buf, plane, v2, u2, nx, ny, nz);
                vertex(pose, buf, plane, v2, u1, nx, ny, nz);
            }
            case EAST -> {  // outward = +X, plane is along X; u=Z, v=Y (reverse wind)
                vertex(pose, buf, plane, v1, u2, nx, ny, nz);
                vertex(pose, buf, plane, v1, u1, nx, ny, nz);
                vertex(pose, buf, plane, v2, u1, nx, ny, nz);
                vertex(pose, buf, plane, v2, u2, nx, ny, nz);
            }
            case UP -> {    // outward = +Y, plane is along Y; u=X, v=Z
                // Verified via (v2-v1)×(v3-v2): this ordering produces normal (0, +1, 0).
                vertex(pose, buf, u1, plane, v1, nx, ny, nz);
                vertex(pose, buf, u1, plane, v2, nx, ny, nz);
                vertex(pose, buf, u2, plane, v2, nx, ny, nz);
                vertex(pose, buf, u2, plane, v1, nx, ny, nz);
            }
            case DOWN -> {  // outward = -Y, plane is along Y; u=X, v=Z
                // Same vertex set, opposite winding — cross product gives (0, -1, 0).
                vertex(pose, buf, u1, plane, v1, nx, ny, nz);
                vertex(pose, buf, u2, plane, v1, nx, ny, nz);
                vertex(pose, buf, u2, plane, v2, nx, ny, nz);
                vertex(pose, buf, u1, plane, v2, nx, ny, nz);
            }
        }
    }

    /**
     * Emit a single vertex. See {@link #emitPanes} for the format conventions.
     */
    private static void vertex(PoseStack.Pose pose, VertexConsumer buf,
                               float x, float y, float z,
                               float nx, float ny, float nz) {
        int r = clampByte(Math.round(clamp01(x) * 255f));
        int g = clampByte(Math.round(clamp01(y) * 255f));
        int b = clampByte(Math.round(clamp01(z) * 255f));
        buf.addVertex(pose, x, y, z)
                .setColor(r, g, b, 255)
                .setNormal(nx, ny, nz);

    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static int clampByte(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
