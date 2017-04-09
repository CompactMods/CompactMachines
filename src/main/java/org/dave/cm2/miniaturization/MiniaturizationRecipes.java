package org.dave.cm2.miniaturization;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.reference.EnumMachineSize;

import java.util.ArrayList;
import java.util.List;

public class MiniaturizationRecipes {
    private static List<MiniaturizationRecipe> recipes = new ArrayList<>();
    private static List<Item> catalystItems = new ArrayList<>();

    public static void init() {
        registerRecipe(new MiniaturizationRecipe(Blocks.GOLD_BLOCK, Items.REDSTONE, 0, 1, 2, 1, Itemss.psd, 1, 0, false));
        registerRecipe(new MiniaturizationRecipe(Blocks.GOLD_BLOCK, Items.REDSTONE, 0, 1, 1, 1, Itemss.tunnelTool, 6, 0, false));
        registerRecipe(new MiniaturizationRecipe(Blocks.IRON_BLOCK, Items.REDSTONE, 0, 3, 1, 3, Blockss.wallBreakable, 64, 0, false));
        for(EnumMachineSize size : EnumMachineSize.values()) {
            int dim = size.getDimension()+1;
            registerRecipe(new MiniaturizationRecipe(Blockss.wallBreakable, Items.ENDER_PEARL, 0, dim, dim, dim, Blockss.machine, 1, size.getMeta(), true));
        }
    }

    public static void registerRecipe(MiniaturizationRecipe recipe) {
        //Logz.info("Registering recipe: %s, width=%d, height=%d, depth=%d --> %s", recipe.getSourceBlock(), recipe.getWidth(), recipe.getHeight(), recipe.getDepth(), recipe.getTargetName());
        recipes.add(recipe);
        catalystItems.add(recipe.getCatalyst());
    }

    public static List<MiniaturizationRecipe> getRecipes() {
        return recipes;
    }

    public static boolean isCatalystItem(Item catalyst) {
        return catalystItems.contains(catalyst);
    }
}
