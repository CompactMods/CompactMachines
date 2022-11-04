package dev.compactmods.machines.room.ui.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import dev.compactmods.machines.client.PlayerFaceRenderer;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.client.Minecraft;
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

        if (!gui.getMinecraft().options.renderDebug)
            return;

        final var font = gui.getFont();
        final var center = screenWidth / 2f;


        player.getCapability(CURRENT_ROOM_META)
                .resolve()
                .flatMap(IPlayerRoomMetadataProvider::currentRoom)
                .ifPresent(room -> {
                    poseStack.pushPose();
                    poseStack.translate(center, screenHeight - 75, 0);

                    PlayerUtil.getProfileByUUID(player.level, room.owner()).ifPresent(ownerInfo -> {
                        final int ownerWidth = font.width(ownerInfo.getName());

//                        final var shader = GameRenderer.getPositionTexShader();
//                        final var floatBackup = shader.COLOR_MODULATOR.getFloatBuffer()
//                                .duplicate();
//
//
//                        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//                        shader.COLOR_MODULATOR.set(1, 1, 1, 0.2f);
//                        shader.COLOR_MODULATOR.upload();

                        PlayerFaceRenderer.render(ownerInfo, poseStack, -6, -14);

//                        shader.COLOR_MODULATOR.getFloatBuffer().rewind();
//                        shader.COLOR_MODULATOR.getFloatBuffer().put(floatBackup);
//                        shader.COLOR_MODULATOR.upload();

                        font.drawShadow(poseStack, ownerInfo.getName(), -(ownerWidth / 2f), 0, 0xFFFFFFFF, false);
                        poseStack.translate(0, 12, 0);
                    });

                    final int codeWidth = font.width(room.roomCode());
                    font.drawShadow(poseStack, room.roomCode(), -(codeWidth / 2f), 0, 0xFFFFFFFF, false);

                    poseStack.popPose();

                });
    }
}
