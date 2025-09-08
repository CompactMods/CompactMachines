package dev.compactmods.machines.room.ui.overlay;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.client.render.CMPlayerFaceRenderer;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.UUID;

public class RoomMetadataDebugOverlay implements GuiLayer {

    private static void drawRoomCode(GuiGraphics graphics, Minecraft mc, Player player) {
        player.getExistingData(CMDataAttachments.CURRENT_ROOM_CODE).ifPresent(code -> {
            graphics.drawCenteredString(mc.font, Component.literal("Current Room: " + code), 0, 0, CommonColors.LIGHT_GRAY);
        });
    }

    private static void drawRoomOwnerInfo(GuiGraphics graphics, Font font, UUID owner) {
        Minecraft mc = Minecraft.getInstance();
        PlayerUtil.getProfileByUUID(mc.level, owner).ifPresent(ownerInfo -> {

            CMPlayerFaceRenderer.render(ownerInfo, graphics, -6, -14, 12);

            final var text = Component.translatable(MachineTranslations.IDs.OWNER, ownerInfo.getName());
            graphics.drawString(font, text,
                    -(font.width(text) / 2), 0, CommonColors.WHITE);
        });
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        final var mc = Minecraft.getInstance();
        if (!mc.getDebugOverlay().showDebugScreen())
            return;

        if (mc.player == null)
            return;

        if (!CompactDimension.isLevelCompact(mc.player.level()))
            return;

        final var screenHeight = mc.getWindow().getGuiScaledHeight();
        final var screenWidth = mc.getWindow().getGuiScaledWidth();

        final var center = screenWidth / 2;

        final var poseStack = graphics.pose();

        poseStack.pushMatrix();
        poseStack.translate(center, screenHeight - 75);

        mc.player.getExistingData(CMDataAttachments.CURRENT_ROOM_CODE)
                .flatMap(CompactMachines::room)
                .flatMap(ri -> ri.getExistingData(CMDataAttachments.ROOM_OWNER))
                .ifPresent(ownerID -> {
                    drawRoomOwnerInfo(graphics, mc.font, ownerID);
                });

        poseStack.translate(0, 12);

        drawRoomCode(graphics, mc, mc.player);

        poseStack.popMatrix();
    }
}
