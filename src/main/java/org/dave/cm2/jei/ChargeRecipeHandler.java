package org.dave.cm2.jei;

import mcjty.lib.jei.CompatRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import org.dave.cm2.item.psd.PSDChargeRecipe;

public class ChargeRecipeHandler extends CompatRecipeHandler<PSDChargeRecipe> {

    public ChargeRecipeHandler() {
        super(VanillaRecipeCategoryUid.CRAFTING);
    }

    @Override
    public Class<PSDChargeRecipe> getRecipeClass() {
        return PSDChargeRecipe.class;
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
