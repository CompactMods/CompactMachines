package dev.compactmods.machines.api.recipe;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShapedWithNbtRecipeBuilder implements RecipeBuilder {
    private final Item result;
    private String group;
    private final int count;
    private final List<String> rows = Lists.newArrayList();
    private final Map<Character, Ingredient> keyMap = Maps.newLinkedHashMap();
    private Advancement.Builder advancement;

    private final List<Consumer<JsonObject>> additionalNbtWriters = Lists.newArrayList();

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
        if (this.keyMap.containsKey(pSymbol)) {
            throw new IllegalArgumentException("Symbol '" + pSymbol + "' is already defined!");
        } else if (pSymbol == ' ') {
            throw new IllegalArgumentException("Symbol ' ' (whitespace) is reserved and cannot be defined");
        } else {
            this.keyMap.put(pSymbol, pIngredient);
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

        pFinishedRecipeConsumer.accept(new ShapedWithNbtRecipeBuilder.Result(this, pRecipeId,
                new ResourceLocation(pRecipeId.getNamespace(), "recipes/" +
                        this.result.getItemCategory().getRecipeFolderName() + "/" + pRecipeId.getPath())));
    }

    public static class Result implements FinishedRecipe {

        private final ShapedWithNbtRecipeBuilder builder;
        private final ResourceLocation id;
        private final Item result;
        private final int count;
        private final String group;
        private final List<String> pattern;
        private final Map<Character, Ingredient> key;
        private final Advancement.Builder advancement;
        private final ResourceLocation advancementId;

        public Result(ShapedWithNbtRecipeBuilder builder, ResourceLocation pId, ResourceLocation advancementId) {
            this.id = pId;
            this.result = builder.result;
            this.count = builder.count;
            this.group = builder.group;
            this.pattern = builder.rows;
            this.key = builder.keyMap;
            this.advancement = builder.advancement;
            this.advancementId = advancementId;
            this.builder = builder;
        }

        @Override
        public void serializeRecipeData(JsonObject json) {
            if (this.group != null && !this.group.isEmpty()) {
                json.addProperty("group", this.group);
            }

            JsonArray pattern = new JsonArray();
            for(var i : this.pattern)
                pattern.add(i);

            json.add("pattern", pattern);


            JsonObject keyMap = new JsonObject();
            for (var keyEntry : this.key.entrySet()) {
                keyMap.add(String.valueOf(keyEntry.getKey()), keyEntry.getValue().toJson());
            }
            json.add("key", keyMap);

            JsonObject result = new JsonObject();
            result.addProperty("item", Registry.ITEM.getKey(this.result).toString());
            if (this.count > 1) {
                result.addProperty("count", this.count);
            }

            this.builder.addResultNbt(result);
            json.add("result", result);
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return RecipeSerializer.SHAPED_RECIPE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return advancement.serializeToJson();
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementId;
        }
    }
}
