package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;

public class ImageSegment extends Segment {
    String location;
    int width;
    int height;

    int textureOffsetX = 0;
    int textureOffsetY = 0;
    int textureWidth = 64;
    int textureHeight = 64;

    boolean centered = false;

    public ImageSegment(String location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
        this.textureWidth = width;
        this.textureHeight = height;
    }

    public void setTextureOffsetX(int textureOffsetX) {
        this.textureOffsetX = textureOffsetX;
    }

    public void setTextureOffsetY(int textureOffsetY) {
        this.textureOffsetY = textureOffsetY;
    }

    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }

    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    @Override
    public void renderSegment(String name, GuiPSDScreen psd, FontRenderer fontRenderer, RenderItem renderItem, int mouseX, int mouseY) {
        psd.mc.getTextureManager().bindTexture(new ResourceLocation(location));

        int imageOffsetX = 0;
        if(centered) {
            imageOffsetX = (227 - width) / 2;
        }

        int x = psd.offsetX + imageOffsetX;
        int y = psd.offsetY;
        int zLevel = 32;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(textureOffsetX + 0) * 0.00390625F), (double)((float)(textureOffsetY + textureHeight) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureOffsetX + textureWidth) * 0.00390625F), (double)((float)(textureOffsetY + textureHeight) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureOffsetX + textureWidth) * 0.00390625F), (double)((float)(textureOffsetY + 0) * 0.00390625F)).endVertex();
        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureOffsetX + 0) * 0.00390625F), (double)((float)(textureOffsetY + 0) * 0.00390625F)).endVertex();
        tessellator.draw();

        psd.offsetY += height;
    }
}
