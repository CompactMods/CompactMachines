package dev.compactmods.machines.api.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class ShapelessRecipeWithNbtResult<T extends ItemWithNbtRecipeBuilder> implements FinishedRecipe {
    private final int count;
    private final String group;
    private final List<Ingredient> ingredients;
    private final Advancement.Builder advancement;
    private final ResourceLocation advancementId;
    private final Consumer<JsonObject> additionalNbtProvider;
    private final T builder;

    public ShapelessRecipeWithNbtResult(T builder, Advancement.Builder adv, ResourceLocation advId,
                                        Consumer<JsonObject> additionalNbtProvider) {
        this.builder = builder;
        this.count = builder.count();
        this.group = builder.group() != null ? builder.group() : "";
        this.ingredients = builder.ingredients();
        this.advancement = adv;
        this.advancementId = advId;
        this.additionalNbtProvider = additionalNbtProvider;
    }

    @Override
    public void serializeRecipeData(JsonObject output) {
        if (this.group != null && !this.group.isEmpty()) {
            output.addProperty("group", this.group);
        }

        JsonArray jsonarray = new JsonArray();

        for (Ingredient ingredient : this.ingredients) {
            jsonarray.add(ingredient.toJson());
        }

        output.add("ingredients", jsonarray);

        JsonObject result = new JsonObject();
        result.addProperty("item", builder.outputItemId().toString());
        if (this.count > 1) {
            result.addProperty("count", this.count);
        }

        JsonObject nbt = new JsonObject();
        additionalNbtProvider.accept(nbt);
        result.add("nbt", nbt);

        output.add("result", result);
    }

    @Override
    public ResourceLocation getId() {
        return builder.outputRecipeId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return RecipeSerializer.SHAPELESS_RECIPE;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return this.advancement.serializeToJson();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return this.advancementId;
    }
}
