package org.dave.cm2.jei;

import mcjty.lib.jei.CompatRecipeHandler;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import org.dave.cm2.miniaturization.MultiblockRecipe;


public class MultiblockRecipeHandler extends CompatRecipeHandler<MultiblockRecipe> {

    public MultiblockRecipeHandler() {
        super(MultiblockRecipeCategory.UID);
    }

    @Override
    public Class<MultiblockRecipe> getRecipeClass() {
        return MultiblockRecipe.class;
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