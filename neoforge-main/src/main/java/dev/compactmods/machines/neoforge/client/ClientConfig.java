package dev.compactmods.machines.neoforge.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static ModConfigSpec CONFIG;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CONFIG = builder.build();
    }
}
