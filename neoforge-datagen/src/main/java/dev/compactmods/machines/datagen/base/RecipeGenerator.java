package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

public class RecipeGenerator extends RecipeProvider {

    public static class Runner extends RecipeProvider.Runner {
        private final String name;

        public Runner(String name, PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
            this.name = name;
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput) {
            return new RecipeGenerator(provider, recipeOutput);
        }

        @Override
        public String getName() {
            return name;
        }
    }
	public RecipeGenerator(HolderLookup.Provider holders, RecipeOutput output) {
		super(holders, output);
	}

    @Override
    protected void buildRecipes() {
		shaped(RecipeCategory.BUILDING_BLOCKS, Rooms.Items.BREAKABLE_WALL.get(), 8)
			.pattern("DDD")
			.pattern("D D")
			.pattern("DDD")
			.define('D', Items.POLISHED_DEEPSLATE)
			.unlockedBy("picked_up_deepslate", has(Tags.Items.COBBLESTONES_DEEPSLATE))
			.save(this.output);

		shaped(RecipeCategory.TOOLS, Shrinking.PERSONAL_SHRINKING_DEVICE.get())
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
			.save(this.output);

		shaped(RecipeCategory.MISC, Shrinking.ENLARGING_MODULE)
			.pattern("BPB")
			.pattern("BEB")
			.pattern("BLB")
			.define('B', Items.STONE_BUTTON)
			.define('P', Items.PISTON)
			.define('E', Items.ENDER_EYE)
			.define('L', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
			.unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
			.save(this.output);

		shaped(RecipeCategory.MISC, Shrinking.SHRINKING_MODULE)
			.pattern("BPB")
			.pattern("BEB")
			.pattern("BLB")
			.define('B', Items.STONE_BUTTON)
			.define('P', Items.STICKY_PISTON)
			.define('E', Items.ENDER_EYE)
			.define('L', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
			.unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
			.save(this.output);
	}
}
