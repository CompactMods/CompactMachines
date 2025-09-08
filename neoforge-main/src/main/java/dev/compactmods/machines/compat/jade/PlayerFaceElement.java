package dev.compactmods.machines.compat.jade;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.client.render.CMPlayerFaceRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.ui.Element;

public class PlayerFaceElement extends Element {

    private final GameProfile profile;

    public PlayerFaceElement(GameProfile profile) {
        this.profile = profile;
        this.width = 12;
        this.height = 12;
    }

    @Override
    public @Nullable Component getNarration() {
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        CMPlayerFaceRenderer.render(profile, guiGraphics, this.getX(), this.getY(), this.height);
    }
}
