package dev.compactmods.machines.client.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.client.gui.widget.AbstractCMGuiWidget;
import dev.compactmods.machines.client.gui.widget.ScrollableWrappedTextWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuidePage implements Widget, GuiEventListener {

    protected final List<AbstractCMGuiWidget> widgets;

    public GuidePage() {
        widgets = new ArrayList<>();

        ScrollableWrappedTextWidget sc = new ScrollableWrappedTextWidget(Constants.MOD_ID + ".psd.pages.machines", 2, 18, 222, 160);
        widgets.add(sc);
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        Font fr = Minecraft.getInstance().font;
        GuiComponent.drawString(ms, fr,
                Component.translatable(Constants.MOD_ID + ".psd.pages.machines.title")
                .withStyle(ChatFormatting.GOLD),
                2, 2, 0);

        for(Widget comp : widgets)
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
