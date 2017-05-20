package org.dave.cm2.item.psd;

import mcjty.lib.compat.CompatIRecipe;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.init.Itemss;

import javax.annotation.Nullable;

public class PSDChargeRecipe implements CompatIRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        int totalFluidDrops = 0;
        boolean hasPsd = false;
        for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if(!ItemStackTools.isValid(stack) || ItemStackTools.isEmpty(stack)) {
                continue;
            }

            Item item = stack.getItem();
            if(item == Itemss.psd) {
                if(hasPsd) {
                    // Only one PSD allowed
                    return false;
                }

                hasPsd = true;
            } else if(item == Itemss.miniFluidDrop) {
                totalFluidDrops++;
            } else {
                return false;
            }
        }

        if(hasPsd && totalFluidDrops > 0) {
            return true;
        }

        return false;
    }

    @Nullable
    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        int totalFluidDrops = 0;
        ItemStack psdCopy = ItemStackTools.getEmptyStack();
        for(int slot = 0; slot < inv.getSizeInventory(); slot++) {
            ItemStack stack = inv.getStackInSlot(slot);
            if(!ItemStackTools.isValid(stack) || ItemStackTools.isEmpty(stack)) {
                continue;
            }

            if(stack.getItem() == Itemss.miniFluidDrop) {
                totalFluidDrops++;
            }

            if(stack.getItem() == Itemss.psd) {
                psdCopy = stack.copy();
            }
        }

        PSDFluidStorage tank = (PSDFluidStorage) psdCopy.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        tank.fill(new FluidStack(Fluidss.miniaturizationFluid, totalFluidDrops * 125), true);
        return psdCopy;
    }

    @Override
    public int getRecipeSize() {
        return 9;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStackTools.getEmptyStack();
    }
}
