package org.dave.compactmachines3.gui.framework;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import org.dave.compactmachines3.gui.framework.event.*;

import java.io.IOException;

public abstract class WidgetGuiContainer extends GuiContainer {
    protected GUI gui;

    private int previousMouseX = Integer.MAX_VALUE;
    private int previousMouseY = Integer.MAX_VALUE;

    public WidgetGuiContainer(Container container) {
        super(container);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        gui.fireEvent(new UpdateScreenEvent());
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(gui.fireEvent(new KeyTypedEvent(typedChar, keyCode)) == WidgetEventResult.CONTINUE_PROCESSING) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(gui.fireEvent(new MouseClickEvent(mouseX, mouseY, mouseButton)) == WidgetEventResult.CONTINUE_PROCESSING) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(gui.fireEvent(new MouseClickMoveEvent(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)) == WidgetEventResult.CONTINUE_PROCESSING) {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(mouseX != previousMouseX || mouseY != previousMouseY) {
            gui.fireEvent(new MouseMoveEvent(mouseX, mouseY));

            previousMouseX = mouseX;
            previousMouseY = mouseY;
        }

        RenderHelper.enableGUIStandardItemLighting();
        gui.drawGUI(this);
        gui.drawTooltips(this, mouseX, mouseY);
        renderHoveredToolTip(mouseX, mouseY);
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        this.drawDefaultBackground();
    }

    protected void resetMousePositions() {
        this.previousMouseX = Integer.MIN_VALUE;
        this.previousMouseY = Integer.MIN_VALUE;
    }
}
