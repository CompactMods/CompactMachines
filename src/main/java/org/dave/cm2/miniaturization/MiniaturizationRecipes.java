package org.dave.cm2.miniaturization;

import net.minecraft.init.Blocks;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.init.Itemss;
import org.dave.cm2.reference.EnumMachineSize;
import org.dave.cm2.utility.Logz;

import java.util.ArrayList;
import java.util.List;

public class MiniaturizationRecipes {
    private static List<MiniaturizationRecipe> recipes = new ArrayList<>();

    public static void init() {
        registerRecipe(new MiniaturizationRecipe(Blocks.GOLD_BLOCK, 1, 2, 1, Itemss.psd, 1, 0, false));
        registerRecipe(new MiniaturizationRecipe(Blocks.GOLD_BLOCK, 1, 1, 1, Itemss.tunnelTool, 6, 0, false));
        registerRecipe(new MiniaturizationRecipe(Blocks.IRON_BLOCK, 3, 1, 3, Blockss.wallBreakable, 64, 0, false));
        for(EnumMachineSize size : EnumMachineSize.values()) {
            int dim = size.getDimension()+1;
            registerRecipe(new MiniaturizationRecipe(Blockss.wallBreakable, dim, dim, dim, Blockss.machine, 1, size.getMeta(), true));
        }
    }

    public static void registerRecipe(MiniaturizationRecipe recipe) {
        Logz.info("Registering recipe: %s, width=%d, height=%d, depth=%d --> %s", recipe.getSourceBlock(), recipe.getWidth(), recipe.getHeight(), recipe.getDepth(), recipe.getTargetName());
        recipes.add(recipe);
    }

    public static List<MiniaturizationRecipe> getRecipes() {
        return recipes;
    }
}
