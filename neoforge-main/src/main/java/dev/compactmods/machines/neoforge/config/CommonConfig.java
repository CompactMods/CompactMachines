package dev.compactmods.machines.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CommonConfig {

    public static ModConfigSpec CONFIG;

    public static ModConfigSpec.BooleanValue ENABLE_VANILLA_RECIPES;



    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder
                .comment("Recipes and Integrations")
                .push("recipes");

        ENABLE_VANILLA_RECIPES = builder
                .comment("Enable vanilla-style recipes.")
                .define("vanillaRecipes", true);

        builder.pop();

        CONFIG = builder.build();
    }
}
