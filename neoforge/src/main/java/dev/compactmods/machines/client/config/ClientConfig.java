package dev.compactmods.machines.client.config;

import dev.compactmods.machines.room.client.RoomClientConfig;
import dev.compactmods.machines.machine.client.MachineClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {

    public static ModConfigSpec CONFIG;

    public static ModConfigSpec.BooleanValue ENABLE_ROOM_PREVIEWS;
    public static ModConfigSpec.DoubleValue PREVIEW_SPAWN_GHOST_OPACITY;

    /**
     * When {@code true}, Compact Machines that haven't been pinned to a
     * specific shader (via the {@code machine_shader} data attachment) will
     * automatically render with the Pride rainbow shader during June.
     */
    public static ModConfigSpec.BooleanValue ENABLE_PRIDE;
    public static ModConfigSpec.ConfigValue<String> DEFAULT_PRIDE_FLAG;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_ROOM_PREVIEWS = builder
                .comment("Enable room preview when opening a bound machine UI")
                .define("enableRoomPreviews", true);

        PREVIEW_SPAWN_GHOST_OPACITY = builder
                .comment("Opacity of the ghost of yourself drawn at your spawn point in the room preview (0 hides it, 1 is solid).")
                .defineInRange("previewSpawnGhostOpacity", 0.5, 0.0, 1.0);

        ENABLE_PRIDE = builder
                .comment("Render Compact Machines with Pride shaders.")
                .define("enablePride", true);

        DEFAULT_PRIDE_FLAG = builder
                .comment("The default pride flag shader ID to use if one is not set on the core item.")
                .define("defaultPrideShader", "compactmachines:pride/baker");

        CONFIG = builder.build();

        // Hand feature-scoped settings to their features, which read them
        // without depending on this class.
        MachineClientConfig.ENABLE_PRIDE = ENABLE_PRIDE;
        MachineClientConfig.DEFAULT_PRIDE_FLAG = DEFAULT_PRIDE_FLAG;
        RoomClientConfig.ENABLE_ROOM_PREVIEWS = ENABLE_ROOM_PREVIEWS;
        RoomClientConfig.PREVIEW_SPAWN_GHOST_OPACITY = PREVIEW_SPAWN_GHOST_OPACITY;
    }
}
