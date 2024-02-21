package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.WallConstants;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.machine.BuiltInRoomTemplate;
import dev.compactmods.machines.neoforge.CompactMachines;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.neoforge.room.Rooms;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

public class RecipeGenerator extends RecipeProvider {

    public RecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, Rooms.ITEM_BREAKABLE_WALL.get(), 8)
                .pattern("DDD")
                .pattern("D D")
                .pattern("DDD")
                .define('D', Items.POLISHED_DEEPSLATE)
                .unlockedBy("picked_up_deepslate", has(Tags.Items.COBBLESTONE_DEEPSLATE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Shrinking.PERSONAL_SHRINKING_DEVICE.get())
                .pattern("121")
                .pattern("345")
                .pattern("676")
                .define('1', Tags.Items.NUGGETS_IRON)
                .define('2', Tags.Items.GLASS_PANES)
                .define('3', Shrinking.ENLARGING_MODULE)
                .define('4', Items.ENDER_EYE)
                .define('5', Shrinking.SHRINKING_MODULE)
                .define('6', Tags.Items.INGOTS_IRON)
                .define('7', Tags.Items.INGOTS_COPPER)
                .unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Shrinking.ENLARGING_MODULE)
                .pattern("BPB")
                .pattern("BEB")
                .pattern("BLB")
                .define('B', Items.STONE_BUTTON)
                .define('P', Items.PISTON)
                .define('E', Items.ENDER_EYE)
                .define('L', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Shrinking.SHRINKING_MODULE)
                .pattern("BPB")
                .pattern("BEB")
                .pattern("BLB")
                .define('B', Items.STONE_BUTTON)
                .define('P', Items.STICKY_PISTON)
                .define('E', Items.ENDER_EYE)
                .define('L', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
                .unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
                .save(recipeOutput);

        addMachineRecipes(recipeOutput);
    }

    private void addMachineRecipes(RecipeOutput consumer) {
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_TINY, Tags.Items.INGOTS_COPPER);
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_SMALL, Tags.Items.INGOTS_IRON);
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_NORMAL, Tags.Items.INGOTS_GOLD);
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_LARGE, Tags.Items.GEMS_DIAMOND);
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_GIANT, Tags.Items.OBSIDIAN);
        registerMachineRecipe(consumer, BuiltInRoomTemplate.EMPTY_COLOSSAL, Tags.Items.INGOTS_NETHERITE);
    }

    protected void registerMachineRecipe(RecipeOutput consumer, BuiltInRoomTemplate template, TagKey<Item> catalyst) {
        registerMachineRecipe(consumer, template.id(), template.template(), catalyst);
    }

    protected void registerMachineRecipe(RecipeOutput consumer, ResourceLocation temId, RoomTemplate template, TagKey<Item> catalyst) {
        final var recipe = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, UnboundCompactMachineItem.forTemplate(temId, template))
                .pattern("WWW")
                .pattern("EPS")
                .pattern("WWW")
                .define('W', Rooms.ITEM_BREAKABLE_WALL)
                .define('E', Shrinking.ENLARGING_MODULE)
                .define('S', Shrinking.SHRINKING_MODULE)
                .define('P', catalyst);

        recipe.unlockedBy("has_recipe", has(Rooms.ITEM_BREAKABLE_WALL.get()));

        final var recipeId = new ResourceLocation(Constants.MOD_ID, "new_machine_" + temId.getPath());
        recipe.save(consumer, recipeId);
    }
}
