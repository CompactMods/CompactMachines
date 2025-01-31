package dev.compactmods.machines.room.ui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.client.render.PlayerFaceRenderer;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;

import javax.swing.*;
import java.util.UUID;

public class RoomMetadataDebugOverlay implements LayeredDraw.Layer {
   
   private static void drawRoomCode(GuiGraphics graphics, Minecraft mc, Player player, PoseStack poseStack, int center, int screenHeight) {
	  player.getExistingData(CMDataAttachments.CURRENT_ROOM_CODE).ifPresent(code -> {
		 graphics.drawCenteredString(mc.font, Component.literal("Current Room: " + code), 0, 0, CommonColors.LIGHT_GRAY);
	  });
   }

   private static void drawRoomOwnerInfo(GuiGraphics graphics, Minecraft mc, Player player, PoseStack poseStack, UUID owner) {
	  PlayerUtil.getProfileByUUID(player.level(), owner).ifPresent(ownerInfo -> {
		 // final int ownerWidth = font.width(ownerInfo.getName());

		 PlayerFaceRenderer.render(ownerInfo, graphics, graphics.pose(), -6, -14);

		 // mc.font.drawShadow(poseStack, ownerInfo.getName(), -(ownerWidth / 2f), 0, 0xFFFFFFFF, false);
		 poseStack.translate(0, 12, 0);
	  });
   }

   @Override
   public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
	  final var mc = Minecraft.getInstance();
	  if (!mc.getDebugOverlay().showDebugScreen())
		 return;

	  if (mc.player == null)
		 return;

	  if(!CompactDimension.isLevelCompact(mc.player.level()))
		 return;

	  final var screenHeight = mc.getWindow().getGuiScaledHeight();
	  final var screenWidth = mc.getWindow().getGuiScaledWidth();

	  final var center = screenWidth / 2;

	  final var poseStack = graphics.pose();

	  poseStack.pushPose();
	  poseStack.translate(center, screenHeight - 75, 0);

	  drawRoomCode(graphics, mc, mc.player, poseStack, center, screenHeight);

	  poseStack.popPose();
   }
}
