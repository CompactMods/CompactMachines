package dev.compactmods.machines.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import dev.compactmods.machines.client.level.EmptyLevelEntityGetter;
import dev.compactmods.machines.core.EnumMachinePlayersBreakHandling;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerConfig {
    public static ForgeConfigSpec CONFIG;

    public static ForgeConfigSpec.EnumValue<EnumMachinePlayersBreakHandling> MACHINE_PLAYER_BREAK_HANDLING;

    public static ForgeConfigSpec.IntValue MACHINE_FLOOR_Y;

    private static ForgeConfigSpec.IntValue REBIND_LEVEL;
    private static ForgeConfigSpec.IntValue GIVE_MACHINE;

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

        MACHINE_FLOOR_Y = builder
                .comment("The Y-dimension to spawn machine floors at.")
                .defineInRange("floor", 40, 10, 200);

        builder.pop();

        builder
                .push("commands")
                .push("permLevels")
                .comment("Specifies requirements for running administrative commands. Requires a server restart to take effect.")
                .comment("0 = ALL, 1 = ADMIN, 2 = OP, 4 = OWNER");


        REBIND_LEVEL = builder.defineInRange("rebind", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);
        GIVE_MACHINE = builder.defineInRange("give", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

        builder.pop(2);

        CONFIG = builder.build();
    }

    public static int rebindLevel() {
        return REBIND_LEVEL.get();
    }

    public static int giveMachineLevel() {
        return GIVE_MACHINE.get();
    }
}
