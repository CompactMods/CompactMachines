package dev.compactmods.machines.neoforge.room.ui.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;

public class RoomMetadataDebugOverlay implements IGuiOverlay {
// FIXME
//    @Override
//    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
//        final var player = Minecraft.getInstance().player;
//        if (player == null || !player.level.dimension().equals(CompactDimension.LEVEL_KEY))
//            return;
//
//        if (!gui.getMinecraft().options.renderDebug)
//            return;
//
//        final var font = gui.getFont();
//        final var center = screenWidth / 2f;
//
//
//        player.getCapability(CURRENT_ROOM_META)
//                .resolve()
//                .flatMap(IPlayerRoomMetadataProvider::currentRoom)
//                .ifPresent(room -> {
//                    poseStack.pushPose();
//                    poseStack.translate(center, screenHeight - 75, 0);
//
//                    PlayerUtil.getProfileByUUID(player.level, room.owner()).ifPresent(ownerInfo -> {
//                        final int ownerWidth = font.width(ownerInfo.getName());
//
//                        PlayerFaceRenderer.render(ownerInfo, poseStack, -6, -14);
//
//                        font.drawShadow(poseStack, ownerInfo.getName(), -(ownerWidth / 2f), 0, 0xFFFFFFFF, false);
//                        poseStack.translate(0, 12, 0);
//                    });
//
//                    final int codeWidth = font.width(room.roomCode());
//                    font.drawShadow(poseStack, room.roomCode(), -(codeWidth / 2f), 0, 0xFFFFFFFF, false);
//
//                    poseStack.popPose();
//
//                });
//    }

    @Override
    public void render(ExtendedGui extendedGui, GuiGraphics guiGraphics, float v, int i, int i1) {

    }
}
