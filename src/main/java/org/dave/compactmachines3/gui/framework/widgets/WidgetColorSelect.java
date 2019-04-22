package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.gui.framework.event.MouseEnterEvent;
import org.dave.compactmachines3.gui.framework.event.MouseExitEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;

import java.awt.*;

public class WidgetColorSelect extends WidgetWithChoiceValue<Color> {
    public boolean hovered = false;

    protected static final ResourceLocation BUTTON_TEXTURES = new ResourceLocation("textures/gui/widgets.png");

    public WidgetColorSelect() {
        this.setId("ColorSelect");
        this.setHeight(20);
        this.setWidth(20);

        this.addListener(MouseEnterEvent.class, (event, widget) -> {((WidgetColorSelect)widget).hovered = true; return WidgetEventResult.CONTINUE_PROCESSING; });
        this.addListener(MouseExitEvent.class, (event, widget) -> {((WidgetColorSelect)widget).hovered = false; return WidgetEventResult.CONTINUE_PROCESSING; });

        this.addClickListener();
    }

    @Override
    public void draw(GuiScreen screen) {
        screen.mc.getTextureManager().bindTexture(BUTTON_TEXTURES);

        float[] colors = this.getValue().getRGBColorComponents(null);
        GlStateManager.color(colors[0], colors[1], colors[2], hovered ? 0.7F : 1.0F);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.translate(0.0f, 0.0f, 2.0f);

        if(hovered) {
            screen.drawTexturedModalRect(0, 0, 0, 46 + 2 * 20, width / 2, height);
            screen.drawTexturedModalRect(width / 2, 0, 200 - width / 2, 46 + 2 * 20, width / 2, height);
        } else {
            screen.drawTexturedModalRect(0, 0, 0, 46 + 1 * 20, width / 2, height);
            screen.drawTexturedModalRect(width / 2, 0, 200 - width / 2, 46 + 1 * 20, width / 2, height);
        }
    }
}