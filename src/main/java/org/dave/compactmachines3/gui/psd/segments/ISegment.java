package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.dave.compactmachines3.gui.GuiPSDScreen;

public interface ISegment {
    boolean isMouseInSegment(int mouseX, int mouseY);
    void renderSegment(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY);
    void renderSegmentInternal(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY);
    void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton);
    void mouseClickedInternal(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton);
}
