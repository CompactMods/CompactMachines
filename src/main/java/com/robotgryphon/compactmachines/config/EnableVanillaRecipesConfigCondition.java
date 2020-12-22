package com.robotgryphon.compactmachines.config;

import com.google.gson.JsonObject;
import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

public class EnableVanillaRecipesConfigCondition implements ICondition {

    public static final ResourceLocation ID = new ResourceLocation(CompactMachines.MOD_ID, "config_enable_vanilla_recipes");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test() {
        return CommonConfig.ENABLE_VANILLA_RECIPES.get();
    }

    public static class Serializer implements IConditionSerializer<EnableVanillaRecipesConfigCondition> {

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public void write(JsonObject json, EnableVanillaRecipesConfigCondition value) {
        }

        @Override
        public EnableVanillaRecipesConfigCondition read(JsonObject json) {
            return new EnableVanillaRecipesConfigCondition();
        }

        @Override
        public ResourceLocation getID() {
            return EnableVanillaRecipesConfigCondition.ID;
        }
    }
}
