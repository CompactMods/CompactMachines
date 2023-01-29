package dev.compactmods.machines.client;

import dev.compactmods.machines.api.core.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {
    public static ForgeConfigSpec CONFIG;

    private static ForgeConfigSpec.BooleanValue ENABLE_ROOM_PREVIEW_ERRORS;
    private static boolean enableRoomPreviewLogging = false;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("logging");

        ENABLE_ROOM_PREVIEW_ERRORS = builder
                .comment("Controls debug logging for the room preview screen.")
                .define("room_preview", false);

        CONFIG = builder.build();
    }

    public static boolean roomPreviewLoggingEnabled() {
        return enableRoomPreviewLogging;
    }

    private static void checkRoomPreview() {
        enableRoomPreviewLogging = ENABLE_ROOM_PREVIEW_ERRORS.get();
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if(!event.getConfig().getModId().equals(Constants.MOD_ID))
            return;

        checkRoomPreview();
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if(!event.getConfig().getModId().equals(Constants.MOD_ID))
            return;

        checkRoomPreview();
    }
}
