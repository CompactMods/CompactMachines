package org.dave.compactmachines3.gui.framework.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.TabChangedEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WidgetTabsPanel extends WidgetPanel {
    private List<WidgetPanel> pages = new ArrayList<>();
    private Map<WidgetPanel, ItemStack> pageStacks = new HashMap<>();
    private Map<WidgetPanel, List<String>> pageTooltips = new HashMap<>();

    private WidgetPanel activePanel = null;

    public WidgetTabsPanel() {
        super();
    }

    public void addPage(WidgetPanel panel, ItemStack buttonStack) {
        this.addPage(panel, buttonStack, null);
    }

    public void addPage(WidgetPanel panel, ItemStack buttonStack, List<String> tooltip) {
        panel.setWidth(this.width);
        panel.setHeight(this.height);

        pages.add(panel);
        pageStacks.put(panel, buttonStack);

        if(activePanel == null) {
            activePanel = panel;
        } else {
            panel.setVisible(false);
        }

        if(tooltip != null) {
            pageTooltips.put(panel, tooltip);
        }

        this.add(panel);
    }

    public void setActivePage(int page) {
        if(page < 0 || page >= pages.size()) {
            return;
        }

        activePanel.setVisible(false);
        pages.get(page).setVisible(true);

        WidgetPanel tmpOld = activePanel;
        activePanel = pages.get(page);

        this.fireEvent(new TabChangedEvent(tmpOld, pages.get(page)));
    }

    public WidgetPanel getButtonsPanel() {
        WidgetPanel result = new WidgetPanel();
        int y = 0;
        for(WidgetPanel page : pages) {
            WidgetTabsButton button = new WidgetTabsButton(this, page, pageStacks.get(page));
            button.setX(0);
            button.setY(y);
            button.setWidth(32);
            button.setHeight(28);
            result.add(button);

            if(pageTooltips.containsKey(page)) {
                button.addTooltipLine(pageTooltips.get(page));
            }

            y += 28;
        }

        return result;
    }

    private static class WidgetTabsButton extends Widget {
        private static ResourceLocation tabIcons = new ResourceLocation(CompactMachines3.MODID, "textures/gui/tabicons.png");
        WidgetTabsPanel parent;
        WidgetPanel page;
        ItemStack pageStack;

        public WidgetTabsButton(WidgetTabsPanel parent, WidgetPanel page, ItemStack pageStack) {
            this.parent = parent;
            this.page = page;
            this.pageStack = pageStack;

            this.addListener(MouseClickEvent.class, (event, widget) -> {
                setActive(true);
                return WidgetEventResult.HANDLED;
            });
        }

        public void setActive(boolean fireEvent) {
            parent.activePanel.setVisible(false);
            page.setVisible(true);
            WidgetPanel tmpOld = parent.activePanel;
            parent.activePanel = page;

            if(fireEvent) {
                this.parent.fireEvent(new TabChangedEvent(tmpOld, page));
            }
        }

        private boolean isActive() {
            return this.parent.activePanel == this.page;
        }

        private boolean isFirst() {
            return this.parent.pages.indexOf(this.page) == 0;
        }

        @Override
        public void draw(GuiScreen screen) {
            GlStateManager.pushMatrix();

            screen.mc.getTextureManager().bindTexture(tabIcons);

            GlStateManager.disableLighting();
            GlStateManager.color(1F, 1F, 1F); //Forge: Reset color in case Items change it.
            GlStateManager.enableBlend(); //Forge: Make sure blend is enabled else tabs show a white border.

            int buttonWidth = 32;
            if(!isActive()) {
                buttonWidth = 28;
            }

            int textureY = isFirst() ? 28 : 28*2;

            screen.drawTexturedModalRect(4, 0, isActive() ? 32 : 0, textureY, buttonWidth, 28);

            RenderHelper.enableGUIStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(pageStack, 12, 5);
            RenderHelper.enableStandardItemLighting();

            GlStateManager.popMatrix();
        }
    }
}
