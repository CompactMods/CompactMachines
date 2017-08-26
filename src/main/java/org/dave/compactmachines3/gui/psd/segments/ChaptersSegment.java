package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;
import org.dave.compactmachines3.gui.psd.Pages;

import java.util.ArrayList;
import java.util.List;

public class ChaptersSegment extends Segment {
    private List<ChapterEntry> chapters = new ArrayList<>();

    @Override
    public void renderSegment(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        for(ChapterEntry chapter : chapters) {
            int color = 0xDDDDDD;
            if(mouseY >= psd.offsetY+1 && mouseY < psd.offsetY+15) {
                color = 0x48cccc;
            }

            RenderHelper.enableStandardItemLighting();
            renderItem.renderItemAndEffectIntoGUI(chapter.getStack(), psd.offsetX, psd.offsetY);
            fontRenderer.drawString(chapter.getLabel(), psd.offsetX+20, psd.offsetY+4, color);

            psd.offsetY += 16;
        }

    }

    public void addChapter(ItemStack itemStack, String label, String targetPage) {
        chapters.add(new ChapterEntry(itemStack, label, targetPage));
    }

    @Override
    public void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {
        int y = 0;
        for(ChapterEntry chapter : chapters) {
            if(mouseY >= y && mouseY < y+15) {
                Pages.activePageOnClient = chapter.getTargetPage();
                break;
            }

            y += 16;
        }
    }
}
