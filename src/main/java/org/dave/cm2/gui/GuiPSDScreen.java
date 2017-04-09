package org.dave.cm2.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.dave.cm2.reference.Resources;

public abstract class GuiPSDScreen extends GuiScreen {
    protected static final ResourceLocation RESOURCE_BACKGROUND = Resources.Gui.PSD_SCREEN;

    protected static final int GUI_WIDTH = 256;
    protected static final int GUI_HEIGHT = 201;

    protected int offsetY;
    protected int offsetX;

    protected boolean displayIntro;
    public String id;

    public GuiPSDScreen(String id) {
        this.id = id;
    }

    public void setDisplayIntro(boolean displayIntro) {
        this.displayIntro = displayIntro;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.renderEngine.bindTexture(RESOURCE_BACKGROUND);
        drawTexturedModalRect(width / 2 - GUI_WIDTH / 2, height - GUI_HEIGHT, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        this.offsetY = (height - GUI_HEIGHT) + 15;
        this.offsetX = (width / 2 - GUI_WIDTH / 2) + 16;

        if(displayIntro) {
            String translated = I18n.format("gui.cm2.psd." + this.id + ".intro");
            String[] lines = translated.split("<br/>");

            for(String line : lines) {
                fontRendererObj.drawString(line, offsetX, this.offsetY, 0xDDDDDD);
                this.offsetY += 10;
            }
        }

        this.drawScreenContent(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    protected abstract void drawScreenContent(int mouseX, int mouseY, float partialTicks);
}
