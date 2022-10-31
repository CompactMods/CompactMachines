package dev.compactmods.machines.api.tunnels.recipe;

import com.google.gson.JsonObject;
import dev.compactmods.machines.api.recipe.ItemWithNbtRecipeBuilder;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.resources.ResourceLocation;

public class TunnelRecipeBuilder extends ItemWithNbtRecipeBuilder<TunnelRecipeBuilder> {

    private TunnelRecipeBuilder(ResourceLocation id, int count) {
        this.recipeId = id;
        this.count = count;
    }

    public static TunnelRecipeBuilder tunnel(ResourceLocation id) {
        return new TunnelRecipeBuilder(id, 1);
    }

    public static TunnelRecipeBuilder tunnel(ResourceLocation id, int count) {
        return new TunnelRecipeBuilder(id, count);
    }

    @Override
    public ResourceLocation outputItemId() {
        return TunnelDefinition.TUNNEL_ID;
    }

    @Override
    public ResourceLocation outputRecipeId() {
        return TunnelRecipeHelper.getRecipeId(this.recipeId);
    }

    public TunnelRecipeBuilder setType(ResourceLocation id) {
        this.recipeId = id;
        return this;
    }

    @Override
    protected void addExtraData(JsonObject root) {
        JsonObject definition = new JsonObject();
        definition.addProperty("id", this.recipeId.toString());
        root.add("definition", definition);
    }

}