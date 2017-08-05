package org.dave.compactmachines3.jei;

import mezz.jei.api.*;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.init.Fluidss;
import org.dave.compactmachines3.init.Itemss;
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
    public void register(IModRegistry registry) {
        registry.addRecipeCategories(new MultiblockRecipeCategory(registry.getJeiHelpers().getGuiHelper()));
        registry.addRecipeHandlers(new MultiblockRecipeHandler());
        registry.addRecipeHandlers(new ChargeRecipeHandler());
        registry.addRecipeCategoryCraftingItem(UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid), MultiblockRecipeCategory.UID);
        registry.addRecipes(MultiblockRecipes.getRecipes());
        registry.addDescription(new ItemStack(Itemss.miniFluidDrop), "compactmachines3.jei.minifluiddrop.description");

        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.tunnel));
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.miniaturizationFluidBlock));
        blacklist.addIngredientToBlacklist(new ItemStack(Blockss.wall));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {

    }
}
