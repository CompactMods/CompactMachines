package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.framework.GUIHelper;
import org.dave.compactmachines3.gui.framework.event.MouseEnterEvent;
import org.dave.compactmachines3.gui.framework.event.MouseExitEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;

public class WidgetSelectButton<T> extends WidgetWithChoiceValue<T> {
    public boolean hovered = false;

    public ResourceLocation backgroundTexture = new ResourceLocation("minecraft", "textures/blocks/concrete_silver.png");
    public TextureAtlasSprite atlasSprite;

    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation(CompactMachines3.MODID, "textures/gui/tabicons.png");

    public WidgetSelectButton() {
        this.setHeight(20);
        this.setWidth(100);

        this.setId("WidgetSelectButton");

        this.addListener(MouseEnterEvent.class, (event, widget) -> {((WidgetSelectButton)widget).hovered = true; return WidgetEventResult.CONTINUE_PROCESSING; });
        this.addListener(MouseExitEvent.class, (event, widget) -> {((WidgetSelectButton)widget).hovered = false; return WidgetEventResult.CONTINUE_PROCESSING; });

        this.addClickListener();
    }

    public WidgetSelectButton<T> setBackgroundTexture(ResourceLocation backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public WidgetSelectButton<T> setAtlasSprite(TextureAtlasSprite atlasSprite) {
        this.atlasSprite = atlasSprite;
        return this;
    }


    @Override
    public void draw(GuiScreen screen) {
        //Logz.info("Width: {}, height: {}", width, height);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.translate(0.0f, 0.0f, 2.0f);

        // Draw the background
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);


        if(atlasSprite != null) {
            screen.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            //screen.drawTexturedModalRect(0, 0, atlasSprite, 16, 16);
            WidgetButton.fillAreaWithIcon(atlasSprite, 0, 0, width, height);
            //Gui.drawModalRectWithCustomSizedTexture(0, 0, atlasSprite.getMinU(), atlasSprite.getMinV(), width, height, atlasSprite.getMaxU()-atlasSprite.getMinU(), atlasSprite.getMaxV()-atlasSprite.getMinU());
        } else {
            screen.mc.getTextureManager().bindTexture(backgroundTexture);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, 16.0f, 16.0f);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, hovered ? 1.0F : 1.0F);
        screen.mc.getTextureManager().bindTexture(BUTTON_TEXTURES);

        // Top Left corner
        int texOffsetX = 64;
        int texOffsetY = 84;
        int overlayWidth = 20;

        screen.drawTexturedModalRect(0, 0, texOffsetX, texOffsetY, 4, 4);


        // Top right corner
        screen.drawTexturedModalRect(0+width - 4, 0, texOffsetX + overlayWidth - 4, texOffsetY, 4, 4);

        // Bottom Left corner
        screen.drawTexturedModalRect(0, this.height - 4, texOffsetX, texOffsetY + overlayWidth - 4, 4, 4);

        // Bottom Right corner
        screen.drawTexturedModalRect(0+width - 4, this.height - 4, texOffsetX + overlayWidth - 4, texOffsetY + overlayWidth - 4, 4, 4);


        // Top edge
        GUIHelper.drawStretchedTexture(0+4, 0, width - 8, 4, texOffsetX + 4, texOffsetY, 12, 4);

        // Bottom edge
        GUIHelper.drawStretchedTexture(0+4, this.height - 4, width - 8, 4, texOffsetX + 4, texOffsetY + overlayWidth - 4, 12, 4);

        // Left edge
        GUIHelper.drawStretchedTexture(0, 4, 4, this.height - 8, texOffsetX, texOffsetY+4, 4, 12);

        // Right edge
        GUIHelper.drawStretchedTexture(0+width - 4, 4, 4, this.height - 8, texOffsetX + overlayWidth - 4, texOffsetY + 3, 4, 12);

        FontRenderer fontrenderer = screen.mc.fontRenderer;
        GlStateManager.translate(0.0f, 0.0f, 10.0f);
        drawButtonContent(screen, fontrenderer);
        GlStateManager.translate(0.0f, 0.0f, -10.0f);

        if(!enabled) {
            GUIHelper.drawColoredRectangle(1, 1, width-2, height-2, 0x80000000);
        } else if(hovered) {
            GUIHelper.drawColoredRectangle(1, 1, width-2, height-2, 0x808090FF);
        }

        GlStateManager.popMatrix();
    }

    protected void drawButtonContent(GuiScreen screen, FontRenderer fontrenderer) {
        int color = 0xEEEEEE;
        screen.drawCenteredString(fontrenderer, getValue().toString(), width / 2, (height - 8) / 2, color);
    }
}
