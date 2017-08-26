package org.dave.compactmachines3.gui.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import org.dave.compactmachines3.tile.TileEntityMachine;

public class GuiMachineContainer extends Container {
    private TileEntityMachine te;

    public GuiMachineContainer(TileEntityMachine te) {
        this.te = te;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }
}
