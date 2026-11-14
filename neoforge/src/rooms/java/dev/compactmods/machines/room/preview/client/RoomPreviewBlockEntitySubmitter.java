package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Submits block entity renderers (chests, signs, modded machines) for a room's rebuilt block
 * entities ({@link PreviewLevel}). The caller's pose must map room-local blocks into its target space.
 *
 * <p>Renderers are called directly rather than through the dispatcher's extraction, which culls by
 * distance from the world camera, not the preview's. Everything is full-bright, as the rest of the
 * preview. A renderer that throws is skipped for its block entity type from then on, so one broken mod
 * renderer can't take the screen down.
 */
public final class RoomPreviewBlockEntitySubmitter {

    private static final Set<BlockEntityType<?>> BROKEN = ConcurrentHashMap.newKeySet();

    private RoomPreviewBlockEntitySubmitter() {}

    /**
     * Block entities live at their world positions (see {@link PreviewLevel}); they're drawn moved to
     * room-local ones.
     *
     * @param camera the preview camera's position, room-local
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void submit(PreviewLevel level, Vec3 camera, PoseStack pose, SubmitNodeCollector collector,
                              CameraRenderState cameraState, float partialTick) {
        final var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        final BlockPos origin = level.origin();
        final Vec3 worldCamera = camera.add(origin.getX(), origin.getY(), origin.getZ());
        for (BlockEntity be : level.blockEntities()) {
            if (BROKEN.contains(be.getType()))
                continue;

            final BlockEntityRenderer renderer = dispatcher.getRenderer(be);
            if (renderer == null)
                continue;

            pose.pushPose();
            try {
                final BlockEntityRenderState state = (BlockEntityRenderState) renderer.createRenderState();
                renderer.extractRenderState(be, state, partialTick, worldCamera, null);
                state.lightCoords = LightCoordsUtil.FULL_BRIGHT;

                final var pos = be.getBlockPos().subtract(origin);
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                dispatcher.submit(state, pose, collector, cameraState);
            } catch (RuntimeException | LinkageError e) {
                BROKEN.add(be.getType());
                CompactMachinesCore.modLog().warn("Room preview: not rendering {} block entities; their renderer failed", be.getType(), e);
            } finally {
                pose.popPose();
            }
        }
    }
}
