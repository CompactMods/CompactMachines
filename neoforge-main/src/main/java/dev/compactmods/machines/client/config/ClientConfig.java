package dev.compactmods.machines.client.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static ModConfigSpec CONFIG;

    public static ModConfigSpec.BooleanValue ENABLE_ROOM_PREVIEWS;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_ROOM_PREVIEWS = builder
                .comment("Enable room preview when opening a bound machine UI")
                .define("enableRoomPreviews", true);

        CONFIG = builder.build();
    }
}
