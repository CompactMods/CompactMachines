package dev.compactmods.machines.room.client.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.util.FastColor;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class RoomMetadataDebugOverlay implements IGuiOverlay {
    private static final Capability<IPlayerRoomMetadataProvider> CURRENT_ROOM_META = CapabilityManager.get(new CapabilityToken<>() {
    });

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        final var player = Minecraft.getInstance().player;
        if (player == null || !player.level.dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        if(!gui.getMinecraft().options.renderDebug)
            return;

        final var font = gui.getFont();
        final var center = screenWidth / 2f;

        poseStack.pushPose();
        poseStack.translate(center, screenHeight - 75, 0);
        int w = font.width("(room meta WIP)");
        font.drawShadow(poseStack, "(room meta WIP)", -(w / 2f), 0, FastColor.ARGB32.color(200, 200, 200, 255));
        poseStack.popPose();

//        player.getCapability(CURRENT_ROOM_META)
//                .resolve()
//                .flatMap(IPlayerRoomMetadataProvider::currentRoom)
//                .ifPresent(room -> {

//
//                    final int ownerWidth = font.width(room.owner().toString());
//                    final int codeWidth = font.width(room.roomCode());
//
//                    PlayerFaceRenderer.render(PlayerFaceRenderer.EMPTY_PROFILE, poseStack, -8, -8);
//
//                    poseStack.translate(0, 20, 0);
//                    font.drawShadow(poseStack, room.owner().toString(), -(ownerWidth / 2f), 0, 0xFFFFFFFF, false);
//                    font.drawShadow(poseStack, room.roomCode(), -(codeWidth / 2f), font.lineHeight + 3, 0xFFFFFFFF, false);
//                });
    }
}
