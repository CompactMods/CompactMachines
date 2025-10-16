package dev.compactmods.machines.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;

public class CMPlayerFaceRenderer {

    public static void render(GameProfile profile, GuiGraphics graphics, int x, int y, int size) {
        final var skins = Minecraft.getInstance().getSkinManager();

        // TODO: Cache?
        final var playerSkin = skins.createLookup(profile, false).get();
        PlayerFaceRenderer.draw(graphics, playerSkin, x, y, size);
    }
}
