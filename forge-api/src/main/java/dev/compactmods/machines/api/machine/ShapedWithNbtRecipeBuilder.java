package dev.compactmods.machines.api.machine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapedWithNbtRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private String group;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> key = Maps.newLinkedHashMap();
    private Advancement.Builder advancement;

    private List<Consumer<JsonObject>> additionalNbtWriters = Lists.newArrayList();

    public ShapedWithNbtRecipeBuilder(ItemLike pResult, int pCount) {
        this.result = pResult.asItem();
        this.count = pCount;
        this.advancement = Advancement.Builder.advancement();
    }

    public static ShapedWithNbtRecipeBuilder shaped(ItemLike out) {
        return new ShapedWithNbtRecipeBuilder(out, 1);
    }

    public ShapedWithNbtRecipeBuilder advancement(Advancement.Builder adv) {
        this.advancement = adv;
        return this;
    }

    public ShapedWithNbtRecipeBuilder addWriter(Consumer<JsonObject> writer) {
        this.additionalNbtWriters.add(writer);
        return this;
    }

    protected void addResultNbt(JsonObject itemTag) {
        additionalNbtWriters.forEach(c -> c.accept(itemTag));
    }

    @Override
    public RecipeBuilder unlockedBy(String criterion, CriterionTriggerInstance trigger) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String newGroup) {
        this.group = newGroup;
        return this;
    }

    public ShapedWithNbtRecipeBuilder define(Character pSymbol, TagKey<Item> pTag) {
        return this.define(pSymbol, Ingredient.of(pTag));
    }

    public ShapedWithNbtRecipeBuilder define(Character pSymbol, ItemLike pItem) {
        return this.define(pSymbol, Ingredient.of(pItem));
    }

    public ShapedWithNbtRecipeBuilder define(Character pSymbol, Ingredient pIngredient) {
        if (this.key.containsKey(pSymbol)) {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        } else if (pSymbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.key.put(pSymbol, pIngredient);
            return this;
        }
    }

    public ShapedWithNbtRecipeBuilder pattern(String pPattern) {
        if (!this.rows.isEmpty() && pPattern.length() != this.rows.get(0).length()) {
            throw new IllegalArgumentException("Pattern must be the same width on every line!");
        } else {
            this.rows.add(pPattern);
            return this;
        }
    }

    @Override
    public Item getResult() {
        return this.result;
    }

    @Override
    public void save(Consumer<FinishedRecipe> pFinishedRecipeConsumer, ResourceLocation pRecipeId) {
        this.advancement.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(pRecipeId))
                .rewards(AdvancementRewards.Builder.recipe(pRecipeId))
                .requirements(RequirementsStrategy.OR);

        pFinishedRecipeConsumer.accept(new ShapedWithNbtRecipeBuilder.Result(this, pRecipeId, this.result, this.count,
                this.group == null ? "" : this.group,
                this.rows, this.key,
                this.advancement,
                new ResourceLocation(pRecipeId.getNamespace(), "recipes/" + this.result.getItemCategory().getRecipeFolderName() + "/" + pRecipeId.getPath())));
    }

    public static class Result extends ShapedRecipeBuilder.Result {

        private final ShapedWithNbtRecipeBuilder builder;

        public Result(ShapedWithNbtRecipeBuilder builder, ResourceLocation pId, Item pResult, int pCount, String pGroup, List<String> pPattern, Map<Character, Ingredient> pKey, Advancement.Builder pAdvancement, ResourceLocation pAdvancementId) {
            super(pId, pResult, pCount, pGroup, pPattern, pKey, pAdvancement, pAdvancementId);
            this.builder = builder;
        }

        @Override
        public void serializeRecipeData(JsonObject pJson) {
            super.serializeRecipeData(pJson);
            this.builder.addResultNbt(pJson.getAsJsonObject("result"));
        }
    }
}
