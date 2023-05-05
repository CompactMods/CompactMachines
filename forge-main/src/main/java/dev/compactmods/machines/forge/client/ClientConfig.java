package dev.compactmods.machines.forge.client;

import dev.compactmods.machines.api.core.Constants;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.BooleanValue SHOW_LEGACY_ITEMS_IN_CREATIVE;
    private static boolean showLegacyItems;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Machines")
                .push("machines");

        SHOW_LEGACY_ITEMS_IN_CREATIVE = builder
                .comment("Show the old machine items in creative/JEI?")
                .comment("Requires re-joining the world/server to take effect.")
                .define("showLegacyItems", false);

        builder.pop();

        CONFIG = builder.build();
    }

    public static boolean showLegacyItems() {
        return showLegacyItems;
    }

    @SubscribeEvent
    public static void onLoadedOrChanged(ModConfigEvent loading) {
        if(loading.getConfig().getModId().equals(Constants.MOD_ID)) {
            showLegacyItems = SHOW_LEGACY_ITEMS_IN_CREATIVE.get();
        }
    }
}
