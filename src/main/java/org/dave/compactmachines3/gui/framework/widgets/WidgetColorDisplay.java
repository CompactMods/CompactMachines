package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class WidgetColorDisplay extends Widget {
    private Color colorA;
    private Color colorB;
    private boolean horizontal;

    public WidgetColorDisplay(Color color) {
        this.colorA = color;
        this.colorB = color;
        this.horizontal = false;
    }

    public WidgetColorDisplay(Color primary, Color secondary, boolean horizontal) {
        this.colorA = primary;
        this.colorB = secondary;
        this.horizontal = horizontal;
    }

    public Color getColor() {
        return colorA;
    }

    public Color getSecondaryColor() {
        return colorB;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public WidgetColorDisplay setColor(Color color) {
        this.colorA = color;
        return this;
    }

    public WidgetColorDisplay setSecondaryColor(Color color) {
        this.colorB = color;
        return this;
    }

    public WidgetColorDisplay setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
        return this;
    }

    @Override
    public void draw(GuiScreen screen) {
        if(isHorizontal()) {
            drawHorizontalGradientRect(0, 0, width, height, colorA, colorB);
        } else {
            drawVerticalGradientRect(0, 0, width, height, colorA, colorB);
        }
    }

    /**
     * Draws a rectangle with a horizontal gradient between the specified colors.
     * x2 and y2 are not included.
     *
     * Copied from McJtyLib
     * https://github.com/McJtyMods/McJtyLib/blob/91606b81e1dace3d6e913505b74704f4e236b3f2/src/main/java/mcjty/lib/client/RenderHelper.java#L339
     */
    private static void drawHorizontalGradientRect(int x1, int y1, int x2, int y2, Color primary, Color secondary) {
        float zLevel = 0.0f;

        float[] pColors = primary.getRGBColorComponents(null);
        float pA = 1.0f;
        float pR = pColors[0];
        float pG = pColors[1];
        float pB = pColors[2];

        float[] sColors = secondary.getRGBColorComponents(null);
        float sA = 1.0f;
        float sR = sColors[0];
        float sG = sColors[1];
        float sB = sColors[2];


        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x1, y1, zLevel).color(pR, pG, pB, pA).endVertex();
        buffer.pos(x1, y2, zLevel).color(pR, pG, pB, pA).endVertex();
        buffer.pos(x2, y2, zLevel).color(sR, sG, sB, sA).endVertex();
        buffer.pos(x2, y1, zLevel).color(sR, sG, sB, sA).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws a rectangle with a vertical gradient between the specified colors.
     * x2 and y2 are not included.
     *
     * Copied from McJtyLib
     * https://github.com/McJtyMods/McJtyLib/blob/91606b81e1dace3d6e913505b74704f4e236b3f2/src/main/java/mcjty/lib/client/RenderHelper.java#L303
     */
    private static void drawVerticalGradientRect(int x1, int y1, int x2, int y2, Color primary, Color secondary) {
        float zLevel = 0.0f;

        float[] pColors = primary.getRGBColorComponents(null);
        float pA = 1.0f;
        float pR = pColors[0];
        float pG = pColors[1];
        float pB = pColors[2];

        float[] sColors = secondary.getRGBColorComponents(null);
        float sA = 1.0f;
        float sR = sColors[0];
        float sG = sColors[1];
        float sB = sColors[2];

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(x2, y1, zLevel).color(pR, pG, pB, pA).endVertex();
        buffer.pos(x1, y1, zLevel).color(pR, pG, pB, pA).endVertex();
        buffer.pos(x1, y2, zLevel).color(sR, sG, sB, sA).endVertex();
        buffer.pos(x2, y2, zLevel).color(sR, sG, sB, sA).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}
