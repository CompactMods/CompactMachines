package dev.compactmods.machines.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import dev.compactmods.machines.api.recipe.ShapelessRecipeWithNbtResult;
import dev.compactmods.machines.api.tunnels.recipe.TunnelRecipeHelper;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Consumer;

public abstract class ItemWithNbtRecipeBuilder<T extends ItemWithNbtRecipeBuilder> {
    protected ResourceLocation recipeId;
    protected final List<Ingredient> ingredients = Lists.newArrayList();
    protected final Advancement.Builder advancement = Advancement.Builder.advancement();
    protected String group;
    protected int count;

    public T requires(ItemLike item) {
        return this.requires(Ingredient.of(item));
    }

    public T requires(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return (T) this;
    }

    public T unlockedBy(String criterionName, CriterionTriggerInstance crit) {
        this.advancement.addCriterion(criterionName, crit);
        return (T) this;
    }

    public T group(String groupName) {
        this.group = groupName;
        return (T) this;
    }

    protected abstract void addExtraData(JsonObject root);

    public void save(Consumer<FinishedRecipe> consumer) {
        this.ensureValid(recipeId);
        this.advancement
                .parent(new ResourceLocation("recipes/root"))
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(TunnelRecipeHelper.getRecipeId(recipeId)))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .requirements(RequirementsStrategy.OR);

        consumer.accept(new ShapelessRecipeWithNbtResult<T>(
                (T) this,
                this.advancement,
                new ResourceLocation(recipeId.getNamespace(), "recipes/" + recipeId.getPath()),
                this::addExtraData
        ));
    }

    private void ensureValid(ResourceLocation recipeId) {
        if (this.advancement.getCriteria().isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + recipeId);
        }
    }

    public abstract ResourceLocation outputItemId();
    public abstract ResourceLocation outputRecipeId();

    public int count() {
        return this.count;
    }

    public String group() {
        return this.group;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }
}
