package dev.compactmods.machines.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import dev.compactmods.machines.core.EnumMachinePlayersBreakHandling;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.EnumValue<EnumMachinePlayersBreakHandling> MACHINE_PLAYER_BREAK_HANDLING;
    public static ForgeConfigSpec.BooleanValue MACHINE_CHUNKLOADING;

    public static ForgeConfigSpec.IntValue MACHINE_FLOOR_Y;

    static {
        generateConfig();
    }

    private static void generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder
                .comment("Machines")
                .push("machines");

        List<String> handling = Arrays
                .stream(EnumMachinePlayersBreakHandling.values())
                .map(v -> String.format(" '%s' = %s", v.configName(), v.configDesc()))
                .collect(Collectors.toList());

        handling.add(0, "Specifies machine breakability while players are inside.");
        String[] handlingFinal = handling.toArray(new String[0]);

        MACHINE_PLAYER_BREAK_HANDLING = builder
                .comment(handlingFinal)
                .defineEnum("breakHandling",
                        EnumMachinePlayersBreakHandling.UNBREAKABLE,
                        EnumGetMethod.NAME_IGNORECASE);

        MACHINE_CHUNKLOADING = builder
                .comment("Allow machines to chunkload their insides when the machines are loaded.")
                .define("chunkloading", true);

        MACHINE_FLOOR_Y = builder
                .comment("The Y-level to spawn machine floors at.")
                .defineInRange("floor", 40, 10, 200);

        builder.pop();

        CONFIG = builder.build();
    }
}
