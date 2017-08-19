package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.dave.compactmachines3.gui.GuiPSDScreen;

public class VerticalSpaceSegment extends Segment {
    private int verticalSpace = 0;

    public VerticalSpaceSegment(int verticalSpace) {
        this.verticalSpace = verticalSpace;
    }

    @Override
    public void renderSegment(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        psd.offsetY += verticalSpace;
    }

    @Override
    public void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {
    }
}
