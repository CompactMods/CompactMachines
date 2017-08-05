package org.dave.compactmachines3.jei;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import mezz.jei.api.recipe.wrapper.ICustomCraftingRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.dave.compactmachines3.init.Fluidss;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.item.psd.PSDFluidStorage;

import java.util.ArrayList;
import java.util.List;

public class ChargeRecipeWrapper extends BlankRecipeWrapper implements ICustomCraftingRecipeWrapper {
    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IIngredients ingredients) {
        recipeLayout.getItemStacks().set(ingredients);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> input = new ArrayList<>();
        input.add(new ItemStack(Itemss.miniFluidDrop));
        input.add(new ItemStack(Itemss.psd));
        ingredients.setInputs(ItemStack.class, input);

        ItemStack resultStack = new ItemStack(Itemss.psd);
        PSDFluidStorage tank = (PSDFluidStorage) resultStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        tank.fill(new FluidStack(Fluidss.miniaturizationFluid, 125), true);
        ingredients.setOutput(ItemStack.class, resultStack);
    }
}
