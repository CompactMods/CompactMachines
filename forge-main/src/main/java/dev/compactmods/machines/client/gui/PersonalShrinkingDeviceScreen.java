package dev.compactmods.machines.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.client.gui.guide.GuideSection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class PersonalShrinkingDeviceScreen extends Screen {
    private final ResourceLocation GUI = new ResourceLocation(Constants.MOD_ID, "textures/gui/psd_screen.png");
    private static final int WIDTH = 256;
    private static final int HEIGHT = 201;

    private final Map<ResourceLocation, GuideSection> sections;
    private final ResourceLocation emptySection = new ResourceLocation(Constants.MOD_ID, "empty");

    @Nullable
    private final GuideSection currentSection;

    protected PersonalShrinkingDeviceScreen() {
        super(Component.translatable(Constants.MOD_ID + ".gui.psd.title"));
        this.sections = new HashMap<>();

        GuideSection root = new GuideSection();
        sections.put(new ResourceLocation(Constants.MOD_ID, "root"), root);
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
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);

        RenderSystem.clearColor(1, 1, 1, 1);
        int relX = (this.width - WIDTH) / 2;

        // relY = relative position, places screen against bottom edge of screen
        int relY = (this.height - HEIGHT);

        matrixStack.pushPose();
        matrixStack.translate(relX, relY, 0);

        RenderSystem.setShaderTexture(0, GUI);
        this.blit(matrixStack, 0, 0, 0, 0, WIDTH, HEIGHT);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(relX + 15, relY + 14, 10);

        if(currentSection != null) {
            currentSection.render(matrixStack, mouseX - relX - 15, mouseY - relY - 14, partialTicks);
        }

        matrixStack.popPose();

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public static void show() {
        Minecraft.getInstance().setScreen(new PersonalShrinkingDeviceScreen());
    }
}
