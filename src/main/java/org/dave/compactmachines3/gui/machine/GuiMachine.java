package org.dave.compactmachines3.gui.machine;

import org.dave.compactmachines3.gui.framework.WidgetGuiContainer;

public class GuiMachine extends WidgetGuiContainer {
    public GuiMachine(GuiMachineContainer container, boolean adminMode) {
        super(container);

        this.xSize = 200;
        this.ySize = 212;

        this.gui = new GuiMachineWidgetGui(this.xSize, this.ySize, container.world, container.pos, container.player, adminMode);
    }
}
