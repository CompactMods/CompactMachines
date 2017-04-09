package org.dave.cm2.gui;

import net.minecraft.client.renderer.RenderHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiPSDSelector extends GuiPSDScreen {
    protected List<GuiPSDSelectorEntry> entries = new ArrayList<>();

    public GuiPSDSelector(String id) {
        super(id);
    }

    public void addEntry(GuiPSDSelectorEntry entry) {
        entries.add(entry);
    }

    @Override
    protected void drawScreenContent(int mouseX, int mouseY, float partialTicks) {
        int y = this.offsetY;
        for(GuiPSDSelectorEntry entry : entries) {
            int color = 0xDDDDDD;
            if(mouseY >= y+1 && mouseY < y+15) {
                color = 0x48cccc;
            }

            RenderHelper.enableStandardItemLighting();
            itemRender.renderItemAndEffectIntoGUI(entry.getStack(), offsetX, y);
            fontRendererObj.drawString(entry.getText(), offsetX+20, y+4, color);

            y += 16;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        int y = this.offsetY;
        for(GuiPSDSelectorEntry entry : entries) {
            if(mouseY >= y+1 && mouseY < y+15) {
                entry.performAction();
                break;
            }

            y += 16;
        }
    }
}
