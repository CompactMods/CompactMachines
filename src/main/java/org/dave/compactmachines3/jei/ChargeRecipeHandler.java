package org.dave.compactmachines3.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import org.dave.compactmachines3.item.psd.PSDChargeRecipe;

public class ChargeRecipeHandler implements IRecipeHandler<PSDChargeRecipe> {

    @Override
    public Class<PSDChargeRecipe> getRecipeClass() {
        return PSDChargeRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(PSDChargeRecipe recipe) {
        return VanillaRecipeCategoryUid.CRAFTING;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(PSDChargeRecipe recipe) {
        return new ChargeRecipeWrapper();
    }

    @Override
    public boolean isRecipeValid(PSDChargeRecipe recipe) {
        return true;
    }
}
