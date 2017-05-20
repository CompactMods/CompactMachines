package org.dave.cm2.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.dave.cm2.miniaturization.MultiblockRecipe;


public class MultiblockRecipeHandler implements IRecipeHandler<MultiblockRecipe> {

    @Override
    public Class<MultiblockRecipe> getRecipeClass() {
        return MultiblockRecipe.class;
    }

    @Override
    public String getRecipeCategoryUid(MultiblockRecipe recipe) {
        return MultiblockRecipeCategory.UID;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(MultiblockRecipe recipe) {
        return new MultiblockRecipeWrapper(recipe);
    }

    @Override
    public boolean isRecipeValid(MultiblockRecipe recipe) {
        return true;
    }
}