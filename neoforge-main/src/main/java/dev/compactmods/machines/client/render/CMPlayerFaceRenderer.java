package dev.compactmods.machines.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CMPlayerFaceRenderer {

    public static void render(GameProfile profile, GuiGraphics graphics, int x, int y, int size) {
        final var skins = Minecraft.getInstance().getSkinManager();
        final var playerSkin = skins.getInsecureSkin(profile);

        // pose, x, y, ???, hatLayer, upsideDown
        net.minecraft.client.gui.components.PlayerFaceRenderer.draw(graphics, playerSkin.texture(), x, y, size, false, false);
    }
}
