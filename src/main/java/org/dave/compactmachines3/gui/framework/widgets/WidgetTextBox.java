package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class WidgetTextBox extends Widget {
    private String text;
    private int textColor = 0xFFFFFF;

    public WidgetTextBox(String text) {
        this.setId("TextBox");
        this.text = text;
        this.setWidth(40);
        this.setHeight(10);
    }

    public WidgetTextBox(String text, int textColor) {
        this.setId("TextBox");
        this.text = text;
        this.textColor = textColor;
        this.setWidth(40);
        this.setHeight(10);
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public void draw(GuiScreen screen) {
        if(text == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        int scale = computeGuiScale(screen.mc);

        int bottomOffset = ((screen.mc.displayHeight/scale) - (getActualY() + height)) * scale;
        GL11.glScissor(getActualX() * scale, bottomOffset, width*scale, height*scale);

        screen.mc.fontRenderer.drawSplitString(text, 0, 0, width, textColor);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }
}
