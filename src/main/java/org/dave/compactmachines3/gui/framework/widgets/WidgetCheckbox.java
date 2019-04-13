package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

public class WidgetCheckbox extends WidgetSelectButton<Boolean> {
    public WidgetCheckbox() {
        this.addChoice(true, false);
        this.setWidth(10);
        this.setHeight(10);

        this.addClickListener();
    }

    @Override
    protected void drawButtonContent(GuiScreen screen, FontRenderer fontrenderer) {
        if(this.getValue()) {
            fontrenderer.drawString("x", 2.2f, 0.3f, 0xEEEEEE, true);
        }
    }
}
