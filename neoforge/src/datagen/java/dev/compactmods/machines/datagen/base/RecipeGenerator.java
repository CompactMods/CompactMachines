package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class RecipeGenerator extends RecipeProvider {

	public RecipeGenerator(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
		super(recipeOutput, advancementOutput);
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

		shaped(RecipeCategory.TOOLS, Shrinking.Items.PERSONAL_SHRINKING_DEVICE.get())
			.pattern("121")
			.pattern("345")
			.pattern("676")
			.define('1', Tags.Items.NUGGETS_IRON)
			.define('2', Tags.Items.GLASS_PANES)
			.define('3', Shrinking.Items.ENLARGING_MODULE)
			.define('4', Items.ENDER_EYE)
			.define('5', Shrinking.Items.SHRINKING_MODULE)
			.define('6', Tags.Items.INGOTS_IRON)
			.define('7', Tags.Items.INGOTS_COPPER)
			.unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
			.save(this.output);
		
		shaped(RecipeCategory.MISC, Shrinking.Items.ENLARGING_MODULE)
			.pattern("BPB")
			.pattern("BEB")
			.pattern("BLB")
			.define('B', Items.STONE_BUTTON)
			.define('P', Items.PISTON)
			.define('E', Items.ENDER_EYE)
			.define('L', Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
			.unlockedBy("picked_up_ender_eye", has(Items.ENDER_EYE))
			.save(this.output);

		shaped(RecipeCategory.MISC, Shrinking.Items.SHRINKING_MODULE)
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
