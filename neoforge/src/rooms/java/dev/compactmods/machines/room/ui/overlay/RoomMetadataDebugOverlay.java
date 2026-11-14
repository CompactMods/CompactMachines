//package dev.compactmods.machines.room.ui.overlay;
//
//import dev.compactmods.machines.api.dimension.CompactDimension;
//import dev.compactmods.machines.room.ui.overlay.CMPlayerFaceRenderer;
//import dev.compactmods.machines.core.util.ProfileUtil;
//import dev.compactmods.machines.room.Rooms;
//import net.minecraft.client.DeltaTracker;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.Font;
//import net.minecraft.client.gui.GuiGraphicsExtractor;
//import net.minecraft.network.chat.Component;
//import net.minecraft.util.CommonColors;
//import net.minecraft.world.entity.player.Player;
//import net.neoforged.neoforge.client.gui.GuiLayer;
//
//import java.util.UUID;
//
//public class RoomMetadataDebugOverlay implements GuiLayer {
//
//    private static void drawRoomCode(GuiGraphicsExtractor graphics, Minecraft mc, Player player) {
//        player.getExistingData(Rooms.DataAttachments.CURRENT_ROOM_CODE).ifPresent(roomCode -> {
//            graphics.text(mc.font, Component.literal("Current Room: " + roomCode), 0, 0, CommonColors.LIGHT_GRAY);
//        });
//    }
//
//    private static void drawRoomOwnerInfo(GuiGraphicsExtractor graphics, Font font, UUID owner) {
//        Minecraft mc = Minecraft.getInstance();
//        ProfileUtil.getProfileByUUID(mc.level, owner).ifPresent(ownerInfo -> {
//
//            CMPlayerFaceRenderer.render(ownerInfo, graphics, -6, -14, 12);
//
//            final var text = Component.translatable(MachineTranslations.IDs.OWNER, ownerInfo.name());
//            graphics.text(font, text,
//                    -(font.width(text) / 2), 0, CommonColors.WHITE);
//        });
//    }
//
//    @Override
//    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
//        final var mc = Minecraft.getInstance();
//        if (!mc.debugEntries.isOverlayVisible())
//            return;
//
//        if (mc.player == null)
//            return;
//
//        if (!CompactDimension.isLevelCompact(mc.player.level()))
//            return;
//
//        final var screenHeight = mc.getWindow().getGuiScaledHeight();
//        final var screenWidth = mc.getWindow().getGuiScaledWidth();
//
//        final var center = screenWidth / 2;
//
//        final var poseStack = graphics.pose();
//
//        poseStack.pushMatrix();
//        poseStack.translate(center, screenHeight - 75);
//
//        mc.player.getExistingData(CMDataAttachments.CURRENT_ROOM_CODE)
//                .flatMap(CompactMachines::room)
//                .flatMap(ri -> ri.getExistingData(CMDataAttachments.ROOM_OWNER))
//                .ifPresent(ownerID -> {
//                    drawRoomOwnerInfo(graphics, mc.font, ownerID);
//                });
//
//        poseStack.translate(0, 12);
//
//        drawRoomCode(graphics, mc, mc.player);
//
//        poseStack.popMatrix();
//    }
//
//    // TODO
////    public static class ScreenEntry implements DebugScreenEntry {
////
////        @Override
////        public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
////
////        }
////
////        @Override
////        public boolean isAllowed(boolean reducedDebugInfo) {
////            return true;
////        }
////
////        @Override
////        public DebugEntryCategory category() {
////            return DebugEntryCategory.RENDERER;
////        }
////    }
//}
