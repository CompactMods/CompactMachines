package com.robotgryphon.compactmachines.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class ScrollableWrappedTextWidget extends AbstractCMGuiWidget {

    private String text;
    private double yScroll = 0;
    private FontRenderer fontRenderer;

    private int maxLinesToShow;
    private int lineIndexStart;
    private List<IReorderingProcessor> lines;
    private int charSize;

    public ScrollableWrappedTextWidget(String text, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.text = text;
        this.fontRenderer = Minecraft.getInstance().fontRenderer;

        this.recalculate();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double temp = yScroll - delta;
        yScroll = MathHelper.clamp(temp, 0, lines.size() - maxLinesToShow - 1);
        recalculate();
        return true;
    }

    private void recalculate() {
        lines = fontRenderer.trimStringToWidth(new StringTextComponent(text), width);

        charSize = fontRenderer.getStringWidth("M");
        int maxOnScreen = height / (charSize + 4);
        maxLinesToShow = Math.min(lines.size(), maxOnScreen);

        // startClamp - either the current line scroll, or the max allowed line
        int startClamp = Math.min((int) Math.floor(yScroll), lines.size());
        lineIndexStart = MathHelper.clamp(0, startClamp, lines.size() - 1);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.push();
        matrixStack.translate(0, 0, 10);

        FontRenderer fr = Minecraft.getInstance().fontRenderer;

        try {
            for (int y = lineIndexStart; y <= lineIndexStart + maxLinesToShow; y++) {
                IReorderingProcessor s = lines.get(y);
                fr.func_238407_a_(matrixStack, s, 0, (y - lineIndexStart) * (charSize + 4), 0xFFFFFF);
            }
        }

        catch(Exception ex1) {}

        matrixStack.pop();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}
