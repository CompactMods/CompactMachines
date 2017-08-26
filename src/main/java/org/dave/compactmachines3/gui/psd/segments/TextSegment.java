package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;

import java.util.List;

public class TextSegment extends Segment {
    private String langId;

    public TextSegment(String langId) {
        this.langId = langId;
    }

    @Override
    public void renderSegment(String pageName, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        String translated = I18n.format("gui.compactmachines3.psd." + pageName + "." + langId);
        translated = translated.replace("\\n", '\n' + "" + '\n');
        List<String> lines = fontRenderer.listFormattedStringToWidth(translated, 227);

        for(String line : lines) {
            fontRenderer.drawString(line, psd.offsetX, psd.offsetY, 0xDDDDDD);
            psd.offsetY += 10;
        }
    }

    @Override
    public void mouseClicked(GuiPSDScreen psd, int mouseX, int mouseY, int mouseButton) {
    }
}
