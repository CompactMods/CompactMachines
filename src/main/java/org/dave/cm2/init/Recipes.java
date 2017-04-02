package org.dave.cm2.init;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Recipes {
    public static void init() {
        registerRecipes();
    }

    private static void registerRecipes() {
        ItemStack outputStack = UniversalBucket.getFilledBucket(ForgeModContainer.getInstance().universalBucket, Fluidss.miniaturizationFluid);
        ItemStack fd = new ItemStack(Itemss.miniFluidDrop);
        ItemStack bucket = new ItemStack(Items.BUCKET);
        GameRegistry.addRecipe(new ShapedRecipes(3, 3, new ItemStack[] {fd, fd, fd, fd, bucket, fd, fd, fd, fd}, outputStack));
    }
}
