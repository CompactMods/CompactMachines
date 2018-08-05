package org.dave.compactmachines3.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.init.Itemss;

public class ShrinkingDeviceUtils {
    public static boolean hasShrinkingDeviceInInventory(EntityPlayer player) {
        for(int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
            ItemStack stack = player.inventory.getStackInSlot(slot);
            if(isShrinkingDevice(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isShrinkingDevice(ItemStack stack) {
        if(stack == null || stack.isEmpty()) {
            return false;
        }

        return stack.getItem() == Itemss.psd;
    }
}
