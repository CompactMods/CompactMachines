package org.dave.cm2.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import org.dave.cm2.init.Fluidss;
import org.dave.cm2.miniaturization.MiniaturizationRecipes;

@JEIPlugin
public class CM2JEIPlugin implements IModPlugin {


    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

    }


    @Override
    public void registerIngredients(IModIngredientRegistration registry) {

    }


    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeCategories(new MultiblockRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeHandlers(new MultiblockRecipeHandler());
        registry.addRecipeCategoryCraftingItem(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid), MultiblockRecipeCategory.UID);
        registry.addRecipes(MiniaturizationRecipes.getRecipes());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
