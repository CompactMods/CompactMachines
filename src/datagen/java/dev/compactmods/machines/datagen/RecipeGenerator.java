package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.tunnels.recipe.TunnelRecipeBuilder;
import dev.compactmods.machines.config.EnableVanillaRecipesConfigCondition;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(Registration.ITEM_BREAKABLE_WALL.get(), 8)
                .pattern("DDD")
                .pattern("D D")
                .pattern("DDD")
                .define('D', Items.POLISHED_DEEPSLATE)
                .unlockedBy("picked_up_deepslate", RecipeProvider.has(Tags.Items.COBBLESTONE_DEEPSLATE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Registration.PERSONAL_SHRINKING_DEVICE.get())
                .pattern(" P ")
                .pattern("EBE")
                .pattern(" I ")
                .define('P', Tags.Items.GLASS_PANES)
                .define('E', Items.ENDER_EYE)
                .define('B', Items.BOOK)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("picked_up_ender_eye", RecipeProvider.has(Items.ENDER_EYE))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.ITEM_TUNNEL_DEF.get(), 2)
                .requires(Ingredient.of(Tags.Items.CHESTS))
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.FLUID_TUNNEL_DEF.get(), 2)
                .requires(Items.BUCKET)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.FORGE_ENERGY.get(), 2)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        addMachineRecipes(consumer);
    }

    private void addMachineRecipes(Consumer<FinishedRecipe> consumer) {
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_TINY.get(), Tags.Items.STORAGE_BLOCKS_COPPER);
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_SMALL.get(), Tags.Items.STORAGE_BLOCKS_IRON);
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_NORMAL.get(), Tags.Items.STORAGE_BLOCKS_GOLD);
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_GIANT.get(), Tags.Items.STORAGE_BLOCKS_DIAMOND);
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_LARGE.get(), Tags.Items.OBSIDIAN);
        registerMachineRecipe(consumer, Registration.MACHINE_BLOCK_ITEM_MAXIMUM.get(), Tags.Items.STORAGE_BLOCKS_NETHERITE);
    }

    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, ItemLike out, TagKey<Item> center) {
        Item wall = Registration.ITEM_BREAKABLE_WALL.get();
        ShapedRecipeBuilder recipe = ShapedRecipeBuilder.shaped(out)
                .pattern("WWW");

        if (center != null)
            recipe.pattern("WCW");
        else
            recipe.pattern("W W");

        recipe.pattern("WWW").define('W', wall);
        if (center != null)
            recipe.define('C', center);

        recipe.unlockedBy("has_recipe", RecipeProvider.has(wall));

        ConditionalRecipe.builder()
                .addCondition(new EnableVanillaRecipesConfigCondition())
                .addRecipe(recipe::save)
                .build(consumer, Objects.requireNonNull(out.asItem().getRegistryName()));
    }
}
