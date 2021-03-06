package com.robotgryphon.compactmachines.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.client.gui.guide.GuideSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PersonalShrinkingDeviceScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(CompactMachines.MOD_ID, "textures/gui/psd_screen.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 201;

    private final Map<ResourceLocation, GuideSection> sections;
    private final ResourceLocation emptySection = new ResourceLocation(CompactMachines.MOD_ID, "empty");

    @Nullable
    private GuideSection currentSection;

    protected PersonalShrinkingDeviceScreen() {
        super(new TranslationTextComponent(CompactMachines.MOD_ID + ".gui.psd.title"));
        this.sections = new HashMap<>();

        GuideSection root = new GuideSection();
        sections.put(new ResourceLocation(CompactMachines.MOD_ID, "root"), root);
        this.currentSection = root;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(currentSection != null)
            currentSection.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int relX = (this.width - WIDTH) / 2;

        // relY = relative position, places screen against bottom edge of screen
        int relY = (this.height - HEIGHT);

        if(currentSection != null)
            return currentSection.mouseScrolled(mouseX - relX - 15, mouseY - relY - 14, delta);

        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        int relX = (this.width - WIDTH) / 2;

        // relY = relative position, places screen against bottom edge of screen
        int relY = (this.height - HEIGHT);

        if(currentSection != null)
            return currentSection.mouseClicked(mouseX - relX - 15, mouseY - relY - 14, button);

        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        RenderSystem.color4f(1, 1, 1, 1);
        int relX = (this.width - WIDTH) / 2;

        // relY = relative position, places screen against bottom edge of screen
        int relY = (this.height - HEIGHT);

        matrixStack.push();
        matrixStack.translate(relX, relY, 0);

        this.minecraft.getTextureManager().bindTexture(GUI);
        this.blit(matrixStack, 0, 0, 0, 0, WIDTH, HEIGHT);
        matrixStack.pop();

        matrixStack.push();
        matrixStack.translate(relX + 15, relY + 14, 10);

        if(currentSection != null) {
            currentSection.render(matrixStack, mouseX - relX - 15, mouseY - relY - 14, partialTicks);
        }

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
