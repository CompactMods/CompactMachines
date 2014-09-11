package org.dave.CompactMachines.inventory;

import org.dave.CompactMachines.utility.ItemHelper;
import org.dave.CompactMachines.utility.LogHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class ContainerCM extends Container {
    protected final int PLAYER_INVENTORY_ROWS = 3;
    protected final int PLAYER_INVENTORY_COLUMNS = 9;

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer)
    {
        return true;
    }
}
