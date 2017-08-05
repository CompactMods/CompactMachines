package org.dave.compactmachines3.jei;

import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IRecipeWrapperFactory;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;

public class MultiblockRecipeWrapperFactory implements IRecipeWrapperFactory<MultiblockRecipe> {
    @Override
    public IRecipeWrapper getRecipeWrapper(MultiblockRecipe recipe) {
        return new MultiblockRecipeWrapper(recipe);
    }
}
