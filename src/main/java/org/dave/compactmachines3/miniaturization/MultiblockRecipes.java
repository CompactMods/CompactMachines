package org.dave.compactmachines3.miniaturization;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.utility.ResourceLoader;
import org.dave.compactmachines3.utility.SerializationHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MultiblockRecipes {
    private static List<MultiblockRecipe> recipes = new ArrayList<>();

    public static List<MultiblockRecipe> getRecipes() {
        return recipes;
    }

    public static void init() {
        loadRecipes();
    }

    public static MultiblockRecipe tryCrafting(World world, BlockPos projectorPos, ItemStack itemStack) {
        if(!(world.getTileEntity(projectorPos) instanceof TileEntityFieldProjector)) {
            return null;
        }

        TileEntityFieldProjector teProjector = (TileEntityFieldProjector)world.getTileEntity(projectorPos);
        List<BlockPos> insideBlocks = teProjector.getInsideBlocks();

        for(MultiblockRecipe recipe : recipes) {
            if(recipe.tryCrafting(world, insideBlocks, itemStack)) {
                return recipe;
            }
        }
        return null;
    }

    public static MultiblockRecipe getRecipeByName(String name) {
        Optional<MultiblockRecipe> opt = recipes.stream().filter(recipe -> recipe.getName().equals(name)).findFirst();
        if(opt.isPresent()) {
            return opt.get();
        }

        return null;
    }

    private static void loadRecipes() {
        ResourceLoader loader = new ResourceLoader(ConfigurationHandler.recipeDirectory, "assets/compactmachines3/config/recipes/");
        for(Map.Entry<String, InputStream> entry : loader.getResources().entrySet()) {
            String filename = entry.getKey();
            InputStream is = entry.getValue();

            if (!filename.endsWith(".json")) {
                continue;
            }

            MultiblockRecipe recipe = SerializationHelper.GSON.fromJson(new JsonReader(new InputStreamReader(is)), MultiblockRecipe.class);
            if (recipe == null) {
                Logz.error("Could not deserialize recipe from file: \"" + filename + "\"");
                continue;
            }

            Logz.info("Loaded recipe \"%s\"", filename);
            recipes.add(recipe);
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
