package dev.compactmods.machines.client;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class PlayerFaceRenderer {
    public static void render(GameProfile profile, PoseStack poseStack, int x, int y) {
        final var skins = Minecraft.getInstance().getSkinManager();
        final var playerSkin = skins.getInsecureSkinLocation(profile);

        RenderSystem.setShaderTexture(0, playerSkin);

        // pose, x, y, ???, hatLayer, upsideDown
        net.minecraft.client.gui.components.PlayerFaceRenderer.draw(poseStack, x, y, 12, false, false);
    }
}
