package com.robotgryphon.compactmachines.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.EnumMachinePlayersBreakHandling;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig {

    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.BooleanValue ENABLE_VANILLA_RECIPES;



    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Recipes and Integrations")
                .push("recipes");

        ENABLE_VANILLA_RECIPES = builder
                .comment("Enable vanilla-style recipes.")
                .define("vanillaRecipes", true);

        builder.pop();

        CONFIG = builder.build();
    }

    @SubscribeEvent
    public static void onLoaded(ModConfig.Loading loading) {
        CompactMachines.LOGGER.debug("Loading common configuration...");
    }
}
