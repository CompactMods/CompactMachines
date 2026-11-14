package dev.compactmods.machines.room.preview.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.client.RoomClientConfig;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Pose;

import java.util.function.Function;

/**
 * A semi-transparent copy of the local player (their skin and player model, from the avatar
 * renderer) standing at their spawn in the previewed room, facing the way they will on arrival.
 *
 * <p>Vanilla's see-through rendering of invisible players uses a fixed ~15% alpha, too faint to
 * pick out in a busy room, so the model is submitted directly with {@link #GHOST} and tinted to the
 * configured opacity ({@link RoomClientConfig#PREVIEW_SPAWN_GHOST_OPACITY}). The model is posed
 * through the avatar renderer's {@code setupAnim}, and placed with the same transforms vanilla's
 * {@code LivingEntityRenderer} applies to a standing player.
 */
final class SpawnGhost {

    /** Avatar model scale, as {@code AvatarRenderer#scale}. */
    private static final float AVATAR_SCALE = 0.9375f;
    /** Model origin offset, as {@code LivingEntityRenderer#submit}. */
    private static final float MODEL_Y_OFFSET = -1.501f;

    /**
     * Translucent entity rendering for one skin texture: vanilla's {@code entity_translucent} setup,
     * without outline or crumbling participation. The alpha comes from the submitted tint.
     */
    private static final Function<Identifier, RenderType> GHOST = Util.memoize(texture -> RenderType.create(
            CompactMachinesCore.dotPrefix("preview_spawn_ghost"),
            RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.NONE)
                    .createRenderSetup()));

    private SpawnGhost() {}

    /** Submits the ghost; {@code pose} maps room-local blocks to the target space. */
    static void submit(ClientRoomPreviews.Spawn spawn, PoseStack pose, SubmitNodeCollector collector, float partialTick) {
        final float opacity = (float) (double) RoomClientConfig.PREVIEW_SPAWN_GHOST_OPACITY.get();
        final var mc = Minecraft.getInstance();
        if (opacity <= 0f || mc.player == null)
            return;

        final var dispatcher = mc.getEntityRenderDispatcher();
        if (!(dispatcher.extractEntity(mc.player, partialTick) instanceof AvatarRenderState state))
            return;

        // Standing still at the spawn, facing as on arrival
        state.bodyRot = spawn.yaw();
        state.yRot = 0f; // head yaw relative to the body
        state.xRot = spawn.pitch();
        state.pose = Pose.STANDING;
        state.isCrouching = false;
        state.isVisuallySwimming = false;
        state.isFallFlying = false;
        state.isPassenger = false;
        state.isUsingItem = false;
        state.isAutoSpinAttack = false;
        state.swimAmount = 0f;
        state.deathTime = 0f;
        state.walkAnimationPos = 0f;
        state.walkAnimationSpeed = 0f;

        final var renderer = dispatcher.getRenderer(state);
        final var model = renderer.getModel();
        model.setupAnim(state);

        final var position = spawn.position();
        pose.pushPose();
        pose.translate(position.x(), position.y(), position.z());
        // LivingEntityRenderer#submit's transforms for a standing, upright entity
        pose.scale(state.scale, state.scale, state.scale);
        pose.rotateDegrees(Axis.YP, 180f - state.bodyRot);
        pose.scale(-1f, -1f, 1f);
        pose.scale(AVATAR_SCALE, AVATAR_SCALE, AVATAR_SCALE);
        pose.translate(0f, MODEL_Y_OFFSET, 0f);

        final int tint = ARGB.color(Mth.floor(Mth.clamp(opacity, 0f, 1f) * 255f), 0xFFFFFF);
        // The full overload: the 7-arg shortcut's last int is the outline colour, its tint is fixed opaque
        collector.submitModel(model, state, pose, GHOST.apply(renderer.getTextureLocation(state)),
                LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint, null, EntityRenderState.NO_OUTLINE);
        pose.popPose();
    }
}
