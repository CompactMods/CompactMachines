package org.dave.compactmachines3.miniaturization;

import com.google.gson.stream.JsonReader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.tile.TileEntityFieldProjector;
import org.dave.compactmachines3.utility.ResourceLoader;
import org.dave.compactmachines3.utility.SerializationHelper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        for (MultiblockRecipe recipe : recipes) {
            if (recipe.getName().equals(name)) {
                return recipe;
            }
        }

        return null;
    }

    private static void loadRecipes() {
        ResourceLoader loader = new ResourceLoader(CompactMachines3.class, ConfigurationHandler.recipeDirectory, "assets/compactmachines3/config/recipes/");
        for(Map.Entry<String, InputStream> entry : loader.getResources().entrySet()) {
            String filename = entry.getKey();
            InputStream is = entry.getValue();

            if (!filename.endsWith(".json")) {
                continue;
            }

            MultiblockRecipe recipe = SerializationHelper.GSON.fromJson(new JsonReader(new InputStreamReader(is)), MultiblockRecipe.class);
            if (recipe == null) {
                CompactMachines3.logger.error("Could not deserialize recipe from file: \"{}\"", filename);
                continue;
            }

            CompactMachines3.logger.info("Loaded recipe \"{}\"", filename);
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
