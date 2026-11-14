package dev.compactmods.machines.datagen.basic_room_templates;


import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.datagen.base.RecipeGenerator;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

import java.util.function.UnaryOperator;

public class BasicRoomTemplateRecipeGenerator extends RecipeGenerator {

    public BasicRoomTemplateRecipeGenerator(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        super(recipeOutput, advancementOutput);
    }

    @Override
    protected void buildRecipes() {
        addMachineRecipe(CompactMachinesCore.identifier("tiny"), Tags.Items.INGOTS_COPPER);
        addMachineRecipe(CompactMachinesCore.identifier("small"), Tags.Items.INGOTS_IRON);
        addMachineRecipe(CompactMachinesCore.identifier("normal"), Tags.Items.INGOTS_GOLD);
        addMachineRecipe(CompactMachinesCore.identifier("large"), Tags.Items.GEMS_DIAMOND);
        addMachineRecipe(CompactMachinesCore.identifier("giant"), Tags.Items.OBSIDIANS);
        addMachineRecipe(CompactMachinesCore.identifier("colossal"), Tags.Items.INGOTS_NETHERITE);

        addMachineRecipe(CompactMachinesCore.identifier("soaryn"), Tags.Items.NETHER_STARS);
        addMachineRecipe(CompactMachinesCore.identifier("farming"), Items.DIAMOND_HOE);
    }

    private void addMachineRecipe(Identifier id, TagKey<Item> catalyst) {
        final var templateRef = this.output.lookup(RoomTemplate.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));

        machineRecipeBuilder(this.output, templateRef, builder -> builder.define('P', catalyst));
    }

    private void addMachineRecipe(Identifier id, ItemLike catalyst) {
        final var templateRef = this.output.lookup(RoomTemplate.REGISTRY_KEY)
                .getOrThrow(ResourceKey.create(RoomTemplate.REGISTRY_KEY, id));

        machineRecipeBuilder(this.output, templateRef, builder -> builder.define('P', catalyst));
    }

    protected void machineRecipeBuilder(RecipeOutput consumer, Holder.Reference<RoomTemplate> templateRef, UnaryOperator<ShapedRecipeBuilder> configure) {
        final var builder = ShapedRecipeBuilder.shaped(this.items,
                        RecipeCategory.MISC, Machines.Items.templateForNewRoom(templateRef))
                .pattern("WWW")
                .pattern("EPS")
                .pattern("WWW")
                .define('W', Rooms.Items.BREAKABLE_WALL)
                .define('E', Shrinking.Items.ENLARGING_MODULE)
                .define('S', Shrinking.Items.SHRINKING_MODULE);

        configure.apply(builder);

        builder.unlockedBy("has_recipe", has(Rooms.Items.BREAKABLE_WALL));
        // Every machine recipe has the same result item, so key it by template instead
        final var templateId = templateRef.key().identifier();
        builder.save(consumer, ResourceKey.create(Registries.RECIPE,
                templateId.withPrefix("machines/")));
    }
}
