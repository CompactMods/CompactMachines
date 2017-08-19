package org.dave.compactmachines3.gui.psd;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import org.dave.compactmachines3.gui.GuiPSDScreen;
import org.dave.compactmachines3.gui.psd.Pages;
import org.dave.compactmachines3.gui.psd.segments.ISegment;

import java.util.ArrayList;
import java.util.List;

public class Page {
    private String name;
    private List<ISegment> segments;

    public Page(String name) {
        this.name = name;
        segments = new ArrayList<>();
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
            if(name == "welcome") {
                Minecraft.getMinecraft().player.closeScreen();
            } else {
                Pages.activePageOnClient = "welcome";
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
