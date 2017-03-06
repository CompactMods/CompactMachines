package org.dave.cm2.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.dave.cm2.miniaturization.MiniaturizationRecipe;


public class MultiblockRecipeHandler implements IRecipeHandler<MiniaturizationRecipe> {

    @Override
    public Class<MiniaturizationRecipe> getRecipeClass() {
        return MiniaturizationRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return MultiblockRecipeCategory.UID;
    }

    @Override
    public String getRecipeCategoryUid(MiniaturizationRecipe recipe) {
        return MultiblockRecipeCategory.UID;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(MiniaturizationRecipe recipe) {
        return new MultiblockRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(MiniaturizationRecipe recipe) {
        return true;
    }
}