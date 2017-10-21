package org.dave.compactmachines3.gui.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class GuiMachineContainer extends Container {
    public GuiMachineContainer() {
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
