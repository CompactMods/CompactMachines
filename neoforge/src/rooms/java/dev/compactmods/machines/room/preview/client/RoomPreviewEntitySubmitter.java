package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * Submits a room's live preview entities ({@link RoomPreviewEntities}) to the feature/submit system,
 * shared by the in-world and GUI previews. The caller's pose must already map room-local blocks into
 * its target space; entities are placed at their room-local feet, full-bright and without shadows
 * (the snapshot carries no light data).
 */
public final class RoomPreviewEntitySubmitter {

    private RoomPreviewEntitySubmitter() {}

    public static void submit(String roomCode, PoseStack pose, SubmitNodeCollector collector,
                              CameraRenderState camera, float partialTick) {
        final var snapshots = ClientRoomEntities.get(roomCode);
        if (snapshots.isEmpty())
            return;

        final var dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        for (RoomEntitySnapshot snapshot : snapshots) {
            final Entity entity = RoomPreviewEntities.get(roomCode, snapshot.entityId());
            if (entity == null) continue;

            final EntityRenderState rs = dispatcher.extractEntity(entity, partialTick);
            rs.lightCoords = LightCoordsUtil.FULL_BRIGHT;
            rs.shadowPieces.clear();
            rs.shadowRadius = 0f;

            // The entity is ticked at 20 Hz; interpolate its room-local feet by partial tick.
            final double ex = Mth.lerp(partialTick, entity.xOld, entity.getX());
            final double ey = Mth.lerp(partialTick, entity.yOld, entity.getY());
            final double ez = Mth.lerp(partialTick, entity.zOld, entity.getZ());

            pose.pushPose();
            pose.translate(ex, ey, ez);
            dispatcher.submit(rs, camera, 0, 0, 0, pose, collector);
            pose.popPose();
        }
    }
}
