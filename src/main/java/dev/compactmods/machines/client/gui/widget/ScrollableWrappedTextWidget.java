package dev.compactmods.machines.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.List;

public class ScrollableWrappedTextWidget extends AbstractCMGuiWidget {

    private final String localeKey;
    private double yScroll = 0;
    private final Font fontRenderer;

    private int maxLinesToShow;
    private int lineIndexStart;
    private List<FormattedCharSequence> lines;
    private int charSize;

    public ScrollableWrappedTextWidget(String key, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.localeKey = key;
        this.fontRenderer = Minecraft.getInstance().font;

        this.recalculate();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double temp = yScroll - delta;
        yScroll = Mth.clamp(temp, 0, lines.size() - maxLinesToShow - 1);
        recalculate();
        return true;
    }

    private void recalculate() {
        String t = I18n.get(localeKey);
        lines = fontRenderer.split(Component.literal(t), width);

        charSize = fontRenderer.width("M");
        int maxOnScreen = height / (charSize + 4);
        maxLinesToShow = Math.min(lines.size(), maxOnScreen);

        // startClamp - either the current line scroll, or the max allowed line
        int startClamp = Math.min((int) Math.floor(yScroll), lines.size());
        lineIndexStart = Mth.clamp(0, startClamp, lines.size() - 1);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, 10);
        
        Font fr = Minecraft.getInstance().font;

        try {
            for (int y = lineIndexStart; y <= lineIndexStart + maxLinesToShow; y++) {
                FormattedCharSequence s = lines.get(y);
                fr.drawShadow(matrixStack, s, 0, (y - lineIndexStart) * (charSize + 4), 0xFFFFFF);
            }
        }

        catch(Exception ex1) {}

        matrixStack.popPose();
    }
}
