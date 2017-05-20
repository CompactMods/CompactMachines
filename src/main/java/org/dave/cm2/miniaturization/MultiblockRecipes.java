package org.dave.cm2.miniaturization;

import com.google.gson.stream.JsonReader;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.cm2.block.BlockMiniaturizationFluid;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.utility.SerializationHelper;
import org.dave.cm2.world.tools.RecursiveSearch;

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
        // 1. Get all connected fluid blocks. This by rules is a layer exact 1 block wide around the crafting
        //    structure. (IDEA: we might be able to stop the search once we found a source block, because that
        //    is all we need?)
        ArrayList<BlockPos> fluidBlocks = new RecursiveSearch(world, contactPos, Blockss.miniaturizationFluidBlock, -1, true).getResult();

        // 2. a) Find the y-highest of any source block inside of this list of fluid blocks, i.e. with meta == 0
        //    b) Find the y-lowest level of any fluid blocks, does not matter if source or not
        BlockPos insidePos = null;
        int lowestY = 255;
        for(BlockPos pos : fluidBlocks) {
            IBlockState state = world.getBlockState(pos);
            if(pos.getY() < lowestY) {
                lowestY = pos.getY();
            }

            IBlockState stateBelow = world.getBlockState(pos.down());
            if(state.getBlock().getMetaFromState(state) == 0 && stateBelow.getBlock() != Blockss.miniaturizationFluidBlock) {
                if(insidePos == null || insidePos.getY() < pos.getY()) {
                    insidePos = pos;
                }
            }
        }

        // No source block with a not mini fluid block below found
        if(insidePos == null) {
            return null;
        }

        // 3. Use the previously gathered information to search for all connected blocks on the inside of the fluid
        ArrayList<BlockPos> insideBlocks = new RecursiveSearch(world, insidePos.down(), Blockss.miniaturizationFluidBlock, lowestY, false).getResult();

        // 4. Find a recipe that matches the given blocks on the inside of the mini fluid
        for(MultiblockRecipe recipe : recipes) {
            if(recipe.tryCrafting(world, insideBlocks, item)) {

                // 5. Drain the fluid
                for(BlockPos fluidPos : fluidBlocks) {
                    IBlockState state = world.getBlockState(fluidPos);
                    if (state.getBlock() == Blockss.miniaturizationFluidBlock && state.getValue(BlockMiniaturizationFluid.LEVEL) == 0) {
                        world.setBlockState(fluidPos, state.withProperty(BlockMiniaturizationFluid.LEVEL, 1));
                    }
                }

                // 6. Remove all blocks
                insideBlocks.forEach(world::setBlockToAir);

                // 7. return success with the itemstack to spawn in the world
                return recipe.getTargetStack();
            }
        }

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
