package org.dave.compactmachines3.init;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.registry.GameRegistry;
//import org.dave.compactmachines3.item.psd.PSDChargeRecipe;

public class Recipes {
    public static void init() {
        registerRecipes();
    }

    private static void registerRecipes() {
        /*
        ItemStack outputStack = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid);
        ItemStack fd = new ItemStack(Itemss.miniFluidDrop, 1, 0);
        ItemStack bucket = new ItemStack(Items.BUCKET, 1, 0);
        GameRegistry.addRecipe(new ShapedRecipes(3, 3, new ItemStack[] {fd, fd, fd, fd, bucket, fd, fd, fd, fd}, outputStack));
        GameRegistry.addRecipe(new PSDChargeRecipe());
        */
    }
}
