package com.robotgryphon.compactmachines.datagen;

import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;

import java.util.Objects;
import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder.shapedRecipe(Registrations.ITEM_BREAKABLE_WALL.get())
                .patternLine(" R ")
                .patternLine(" I ")
                .key('R', Tags.Items.DUSTS_REDSTONE)
                .key('I', Tags.Items.STORAGE_BLOCKS_IRON)
                .addCriterion("has_recipe", hasItem(Tags.Items.STORAGE_BLOCKS_IRON))
                .build(consumer);

        ShapedRecipeBuilder.shapedRecipe(Registrations.PERSONAL_SHRINKING_DEVICE.get())
                .patternLine(" P ")
                .patternLine("EBE")
                .patternLine(" I ")
                .key('P', Tags.Items.GLASS_PANES)
                .key('E', Items.ENDER_EYE)
                .key('B', Items.BOOK)
                .key('I', Tags.Items.INGOTS_IRON)
                .addCriterion("has_recipe", hasItem(Items.ENDER_EYE))
                .build(consumer);
        
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_TINY.get(), ItemTags.PLANKS);
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_SMALL.get(), Tags.Items.STORAGE_BLOCKS_IRON);
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_NORMAL.get(), Tags.Items.STORAGE_BLOCKS_GOLD);
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_GIANT.get(), Tags.Items.STORAGE_BLOCKS_DIAMOND);
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_LARGE.get(), Tags.Items.OBSIDIAN);
        registerMachineRecipe(consumer, Registrations.MACHINE_BLOCK_ITEM_MAXIMUM.get(), Tags.Items.STORAGE_BLOCKS_EMERALD);
    }

    protected void registerMachineRecipe(Consumer<IFinishedRecipe> consumer, IItemProvider out, ITag<Item> center) {
        Item wall = Registrations.ITEM_BREAKABLE_WALL.get();
        ShapedRecipeBuilder recipe = ShapedRecipeBuilder.shapedRecipe(out)
                .patternLine("WWW");

        if (center != null)
            recipe.patternLine("WCW");
        else
            recipe.patternLine("W W");

        recipe.patternLine("WWW").key('W', wall);
        if (center != null)
            recipe.key('C', center);

        recipe
                .addCriterion("has_recipe", hasItem(wall));

        ConditionalRecipe.builder()
                .addCondition(new NotCondition(new ModLoadedCondition("compactcrafting")))
                .addRecipe(recipe::build)
                .build(consumer, Objects.requireNonNull(out.asItem().getRegistryName()));
    }
}
