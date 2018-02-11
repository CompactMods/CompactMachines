package org.dave.compactmachines3.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.miniaturization.MultiblockRecipe;
import org.dave.compactmachines3.miniaturization.MultiblockRecipes;

@JEIPlugin
public class CompactMachines3JEIPlugin implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new MultiblockRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeCatalyst(new ItemStack(Blockss.fieldProjector), MultiblockRecipeCategory.UID);

        registry.handleRecipes(MultiblockRecipe.class, recipe -> new MultiblockRecipeWrapper(recipe), MultiblockRecipeCategory.UID);
        registry.addRecipes(MultiblockRecipes.getRecipes(), MultiblockRecipeCategory.UID);

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.tunnel));
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.redstoneTunnel));
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.wall));
    }
}
