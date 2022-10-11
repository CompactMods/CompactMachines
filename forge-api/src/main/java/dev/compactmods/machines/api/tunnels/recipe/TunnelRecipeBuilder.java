package dev.compactmods.machines.api.tunnels.recipe;

import com.google.gson.JsonObject;
import dev.compactmods.machines.api.ItemWithNbtRecipeBuilder;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class TunnelRecipeBuilder extends ItemWithNbtRecipeBuilder<TunnelRecipeBuilder> {

    private TunnelRecipeBuilder(RegistryObject<TunnelDefinition> definition, int count) {
        this.recipeId = definition.getId();
        this.count = count;
    }

    @Override
    public ResourceLocation outputItemId() {
        return TunnelDefinition.TUNNEL_ID;
    }

    @Override
    public ResourceLocation outputRecipeId() {
        return TunnelRecipeHelper.getRecipeId(this.recipeId);
    }

    public static TunnelRecipeBuilder tunnel(RegistryObject<TunnelDefinition> definition) {
        return new TunnelRecipeBuilder(definition, 1);
    }

    public static TunnelRecipeBuilder tunnel(RegistryObject<TunnelDefinition> definition, int count) {
        return new TunnelRecipeBuilder(definition, count);
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