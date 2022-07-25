package dev.compactmods.machines.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
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
    private static ForgeConfigSpec.IntValue CHANGE_SPAWN_LEVEL;

    private static ForgeConfigSpec.IntValue CHANGE_ROOM_UPGRADES;

    private static ForgeConfigSpec.BooleanValue ALLOWED_OUTSIDE_MACHINE;

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

        ALLOWED_OUTSIDE_MACHINE = builder
                .comment("Specify if we want to damage player's that managed to escape the boundries")
                .comment("default: false")
                .define("allowOutside", false);

        builder.pop();

        builder
                .push("commands")
                .push("permLevels")
                .comment("Specifies requirements for running administrative commands. Requires a server restart to take effect.")
                .comment("0 = ALL, 1 = ADMIN, 2 = OP, 4 = OWNER");


        REBIND_LEVEL = builder
                .comment("Command level required for using the rebind and unbind commands.")
                .defineInRange("rebind", Commands.LEVEL_GAMEMASTERS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

        GIVE_MACHINE = builder
                .comment("Command level required for giving new machines to players.")
                .defineInRange("give", Commands.LEVEL_GAMEMASTERS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

        CHANGE_SPAWN_LEVEL = builder
                .comment("Command level required for changing room spawn information.")
                .defineInRange("spawn", Commands.LEVEL_GAMEMASTERS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);

        CHANGE_ROOM_UPGRADES = builder
                .comment("Command level required for changing room upgrades.")
                .defineInRange("upgrades", Commands.LEVEL_GAMEMASTERS, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);


        builder.pop(2);

        CONFIG = builder.build();
    }

    public static int rebindLevel() {
        return REBIND_LEVEL.get();
    }

    public static int giveMachineLevel() {
        return GIVE_MACHINE.get();
    }

    public static int changeRoomSpawn() {
        return CHANGE_SPAWN_LEVEL.get();
    }

    public static int changeUpgrades() { return CHANGE_ROOM_UPGRADES.get(); }

    public static Boolean isAllowedOutsideOfMachine() {
        return ALLOWED_OUTSIDE_MACHINE.get();
    }
}
