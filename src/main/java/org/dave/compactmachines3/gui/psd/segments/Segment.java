package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.dave.compactmachines3.gui.GuiPSDScreen;

class Segment implements ISegment {
    public int startY = 0;
    public int endY = 0;

    @Override
    public void renderSegmentInternal(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        startY = psd.offsetY;
        renderSegment(name, psd, fontRenderer, renderItem, mouseX, mouseY);
        endY = psd.offsetY;
    }

    public void mouseClickedInternal(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {
        mouseClicked(psd, mouseX, mouseY-startY, mouseButton);
    }

    @Override
    public void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {

    }

    @Override
    public boolean isMouseInSegment(int mouseX, int mouseY) {
        return mouseY >= startY && mouseY < endY;
    }

    @Override
    public void renderSegment(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {

    }
}
