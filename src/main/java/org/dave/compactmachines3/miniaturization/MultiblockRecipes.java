package org.dave.compactmachines3.miniaturization;

import com.google.gson.stream.JsonReader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.SerializationHelper;
import org.dave.compactmachines3.world.tools.RecursiveSearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class MultiblockRecipes {
    private static List<MultiblockRecipe> recipes = new ArrayList<>();

    public static List<MultiblockRecipe> getRecipes() {
        return recipes;
    }

    public static void init() {
        loadRecipes();
    }

    public static ItemStack tryCrafting(World world, BlockPos contactPos, Item item) {
        // TODO: All of this is broken obviously
        return ItemStack.EMPTY;
    }

    private static void loadRecipes() {
        // First load local recipes, keep track of what we loaded though
        ArrayList<String> loadedLocalRecipes = new ArrayList<>();
        if(ConfigurationHandler.recipeDirectory.exists()) {
            for (File file : ConfigurationHandler.recipeDirectory.listFiles()) {
                MultiblockRecipe recipe = null;
                try {
                    recipe = SerializationHelper.GSON.fromJson(new JsonReader(new FileReader(file)), MultiblockRecipe.class);
                } catch (FileNotFoundException e) {
                }

                if (recipe == null) {
                    Logz.error("Could not deserialize recipe from file: \"" + file.getPath() + "\"");
                    continue;
                }

                Logz.info("Loaded recipe \"%s\" from config folder", file.getName());
                recipes.add(recipe);
                loadedLocalRecipes.add(file.getName());
            }
        }
    }

    public static boolean isCatalystItem(Item item) {
        for(MultiblockRecipe recipe : getRecipes()) {
            if(recipe.getCatalystStack().getItem() == item) {
                return true;
            }
        }

        return false;
    }
}
