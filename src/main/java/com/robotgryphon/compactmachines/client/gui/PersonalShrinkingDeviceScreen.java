package com.robotgryphon.compactmachines.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class PersonalShrinkingDeviceScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(CompactMachines.MOD_ID, "textures/gui/psd_screen.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 201;

    protected PersonalShrinkingDeviceScreen() {
        super(new TranslationTextComponent(CompactMachines.MOD_ID + ".gui.psd.title"));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        
        RenderSystem.color4f(1, 1, 1, 1);
        int relX = (this.width - WIDTH) / 2;
        int relY = (this.height - HEIGHT) / 2;

        matrixStack.push();
        matrixStack.translate(relX, relY, 0);

        this.minecraft.getTextureManager().bindTexture(GUI);
        this.blit(matrixStack, 0, 0, 0, 0, WIDTH, HEIGHT);

        matrixStack.pop();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void show() {
        Minecraft.getInstance().displayGuiScreen(new PersonalShrinkingDeviceScreen());
    }
}
