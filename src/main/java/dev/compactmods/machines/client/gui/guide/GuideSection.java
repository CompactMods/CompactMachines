package dev.compactmods.machines.client.gui.guide;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Widget;

import java.util.ArrayList;
import java.util.List;

public class GuideSection implements Widget, GuiEventListener {
    private final List<GuidePage> pages;
    private final int currentPageIndex = 0;
    private final GuidePage currentPage;

    public GuideSection() {
        this.pages = new ArrayList<>();
        this.currentPage = new GuidePage();
        this.pages.add(currentPage);
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if(this.currentPage != null)
            currentPage.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(this.currentPage != null)
            currentPage.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(this.currentPage != null)
            return currentPage.mouseClicked(mouseX, mouseY, button);

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(this.currentPage != null)
            return currentPage.mouseScrolled(mouseX, mouseY, delta);

        return false;
    }
}
