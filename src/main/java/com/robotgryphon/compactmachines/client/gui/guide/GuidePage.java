package com.robotgryphon.compactmachines.client.gui.guide;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.client.gui.widget.AbstractCMGuiWidget;
import com.robotgryphon.compactmachines.client.gui.widget.ScrollableWrappedTextWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuidePage implements IRenderable, IGuiEventListener {

    protected List<AbstractCMGuiWidget> widgets;

    public GuidePage() {
        widgets = new ArrayList<>();

        ScrollableWrappedTextWidget sc = new ScrollableWrappedTextWidget(CompactMachines.MOD_ID + ".psd.pages.machines", 2, 18, 222, 160);
        widgets.add(sc);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        FontRenderer fr = Minecraft.getInstance().font;
        AbstractGui.drawString(ms, fr,
                new TranslationTextComponent(CompactMachines.MOD_ID + ".psd.pages.machines.title")
                .withStyle(TextFormatting.GOLD),
                2, 2, 0);

        for(IRenderable comp : widgets)
            comp.render(ms, mouseX, mouseY, partialTicks);
    }

    public Optional<AbstractCMGuiWidget> getWidgetByPosition(double mouseX, double mouseY) {
        for(AbstractCMGuiWidget wid : widgets) {
            if(wid.isMouseOver(mouseX, mouseY))
                return Optional.of(wid);
        }

        return Optional.empty();
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        getWidgetByPosition(mouseX, mouseY)
                .ifPresent(c -> c.mouseMoved(mouseX, mouseY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return getWidgetByPosition(mouseX, mouseY)
                .map(c -> c.mouseScrolled(mouseX, mouseY, delta))
                .orElse(false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return getWidgetByPosition(mouseX, mouseY)
                .map(c -> c.mouseClicked(mouseX, mouseY, button))
                .orElse(false);
    }
}
