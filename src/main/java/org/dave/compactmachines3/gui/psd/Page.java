package org.dave.compactmachines3.gui.psd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.dave.compactmachines3.gui.psd.segments.ISegment;

import java.util.ArrayList;
import java.util.List;

public class Page {
    protected final Pages pages;
    private final String name;
    private final List<ISegment> segments;

    public Page(Pages pages, String name) {
        this.pages = pages;
        this.name = name;
        this.segments = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addSegment(ISegment segment) {
        segments.add(segment);
    }

    public void draw(GuiPSDScreen guiPSDScreen, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        for(ISegment segment : segments) {
            segment.renderSegmentInternal(name, guiPSDScreen, fontRenderer, renderItem, mouseX, mouseY);
        }
    }

    public void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 1) {
            if(name.equals("welcome")) {
                Minecraft.getMinecraft().player.closeScreen();
            } else {
                pages.setActivePage("welcome");
            }

            return;
        }

        for(ISegment segment : segments) {
            if(segment.isMouseInSegment(mouseX, mouseY)) {
                segment.mouseClickedInternal(psd, mouseX, mouseY, mouseButton);
            }
        }
    }
}
