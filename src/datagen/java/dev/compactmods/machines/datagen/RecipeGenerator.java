package dev.compactmods.machines.datagen;

import com.google.gson.JsonObject;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.machine.ShapedWithNbtRecipeBuilder;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.tunnels.recipe.TunnelRecipeBuilder;
import dev.compactmods.machines.machine.LegacySizedTemplates;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.data.MachineDataTagBuilder;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.tunnel.BuiltinTunnels;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

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

        TunnelRecipeBuilder.tunnel(BuiltinTunnels.ITEM_TUNNEL_DEF, 2)
                .requires(Ingredient.of(Tags.Items.CHESTS))
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(BuiltinTunnels.FLUID_TUNNEL_DEF, 2)
                .requires(Items.BUCKET)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        TunnelRecipeBuilder.tunnel(BuiltinTunnels.FORGE_ENERGY, 2)
                .requires(Items.GLOWSTONE_DUST)
                .requires(Items.ENDER_PEARL)
                .requires(Items.REDSTONE)
                .requires(Items.OBSERVER)
                .unlockedBy("observer", RecipeProvider.has(Items.OBSERVER))
                .save(consumer);

        addMachineRecipes(consumer);
    }

    private void addMachineRecipes(Consumer<FinishedRecipe> consumer) {
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_TINY, Tags.Items.STORAGE_BLOCKS_COPPER);
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_SMALL, Tags.Items.STORAGE_BLOCKS_IRON);
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_NORMAL, Tags.Items.STORAGE_BLOCKS_GOLD);
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_LARGE, Tags.Items.STORAGE_BLOCKS_DIAMOND);
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_GIANT, Tags.Items.OBSIDIAN);
        registerMachineRecipe(consumer, LegacySizedTemplates.EMPTY_COLOSSAL, Tags.Items.INGOTS_NETHERITE);

        registerMachineRecipe(consumer, new ResourceLocation(Constants.MOD_ID, "absurd"),
                new RoomTemplate(25, FastColor.ARGB32.color(255, 0, 166, 88)),
                Tags.Items.NETHER_STARS);
    }

    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, LegacySizedTemplates template, TagKey<Item> center) {
        registerMachineRecipe(consumer, template.id(), template.template(), center);
    }

    protected void registerMachineRecipe(Consumer<FinishedRecipe> consumer, ResourceLocation temId, RoomTemplate tem, TagKey<Item> center) {
        Item wall = Walls.ITEM_BREAKABLE_WALL.get();
        ShapedWithNbtRecipeBuilder recipe = ShapedWithNbtRecipeBuilder.shaped(Machines.UNBOUND_MACHINE_BLOCK_ITEM.get())
                .pattern("WWW");

        if (center != null)
            recipe.pattern("WCW");
        else
            recipe.pattern("W W");

        recipe.pattern("WWW").define('W', wall);
        if (center != null)
            recipe.define('C', center);

        recipe.unlockedBy("has_recipe", RecipeProvider.has(wall));
        recipe.addWriter(r -> {
            final var nbt = new JsonObject();
            MachineDataTagBuilder.forTemplate(temId, tem)
                    .writeToItemJson(nbt)
                    .writeToBlockDataJson(nbt);
            r.add("nbt", nbt);
        });

        final var recipeId = new ResourceLocation(Constants.MOD_ID, "new_machine_" + temId.getPath());
        recipe.save(consumer, recipeId);
    }
}
