package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.tunnels.recipe.TunnelRecipeBuilder;
import dev.compactmods.machines.config.EnableVanillaRecipesConfigCondition;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(Walls.ITEM_BREAKABLE_WALL.get(), 8)
                .pattern("DDD")
                .pattern("D D")
                .pattern("DDD")
                .define('D', Items.POLISHED_DEEPSLATE)
                .unlockedBy("picked_up_deepslate", RecipeProvider.has(Tags.Items.COBBLESTONE_DEEPSLATE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(Shrinking.PERSONAL_SHRINKING_DEVICE.get())
                .pattern(" P ")
                .pattern("EBE")
                .pattern(" I ")
                .define('P', Tags.Items.GLASS_PANES)
                .define('E', Items.ENDER_EYE)
                .define('B', Items.BOOK)
                .define('I', Tags.Items.INGOTS_IRON)
                .unlockedBy("picked_up_ender_eye", RecipeProvider.has(Items.ENDER_EYE))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.ITEM_TUNNEL_DEF, 2)
                .requires(Ingredient.of(Tags.Items.CHESTS))
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.FLUID_TUNNEL_DEF, 2)
                .requires(Items.BUCKET)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(Tunnels.FORGE_ENERGY, 2)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        addMachineRecipes(consumer);
    }

    private void addMachineRecipes(Consumer<FinishedRecipe> consumer) {
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_TINY.get(), Tags.Items.STORAGE_BLOCKS_COPPER);
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_SMALL.get(), Tags.Items.STORAGE_BLOCKS_IRON);
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_NORMAL.get(), Tags.Items.STORAGE_BLOCKS_GOLD);
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_GIANT.get(), Tags.Items.STORAGE_BLOCKS_DIAMOND);
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_LARGE.get(), Tags.Items.OBSIDIAN);
        registerMachineRecipe(consumer, Machines.MACHINE_BLOCK_ITEM_MAXIMUM.get(), Tags.Items.STORAGE_BLOCKS_NETHERITE);
    }

    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, ItemLike out, TagKey<Item> center) {
        Item wall = Walls.ITEM_BREAKABLE_WALL.get();
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
                .build(consumer, Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(out.asItem())));
    }
}
