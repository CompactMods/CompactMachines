package com.robotgryphon.compactmachines.client.gui.guide;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.robotgryphon.compactmachines.client.gui.widget.AbstractCMGuiWidget;
import com.robotgryphon.compactmachines.client.gui.widget.ScrollableWrappedTextWidget;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuidePage implements IRenderable, IGuiEventListener {

    protected List<AbstractCMGuiWidget> widgets;

    public GuidePage() {
        widgets = new ArrayList<>();

        String ex = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse posuere nunc enim, " +
                "nec euismod libero ornare accumsan. Nulla dapibus eros lectus, et pellentesque magna finibus ut. " +
                "Quisque ornare id sem id luctus. Nam pulvinar dolor purus.\n\nFusce interdum, nisl vitae congue " +
                "feugiat, enim erat porttitor lorem, eu iaculis lorem est sed quam. Aliquam condimentum sed " +
                "dolor ut lobortis. Nunc eget turpis eget ligula malesuada volutpat. Aliquam nec magna nec massa " +
                "varius finibus.";

        ex += "Nunc sollicitudin pellentesque interdum. In tempus, eros sed tincidunt semper, lorem ante volutpat mi, at sodales mauris nulla eget orci. Ut fermentum eros et massa condimentum tempus. Donec et convallis sem. In et ante non elit vehicula sagittis. Fusce sed nunc mauris. Vivamus at leo condimentum sem mollis pharetra vitae sit amet justo. Proin ac felis porta, consectetur tellus eget, volutpat dui. Vivamus porttitor gravida odio, vel sodales velit tempor at. Quisque lorem mauris, sagittis eget sem sit amet, iaculis elementum sem. Sed sed nibh quis tortor mollis ornare gravida eget nibh. Cras consectetur elit eros, dignissim hendrerit metus luctus vitae. Fusce ante orci, sollicitudin ut tempor et, fermentum id velit.";

        ScrollableWrappedTextWidget sc = new ScrollableWrappedTextWidget(ex, 0, 0, 226, 100);
        widgets.add(sc);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
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
