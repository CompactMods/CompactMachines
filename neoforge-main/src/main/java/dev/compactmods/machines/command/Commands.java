package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.command.rooms.CMRoomsSubcommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Commands {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    static final LiteralArgumentBuilder<CommandSourceStack> CM_COMMAND_ROOT
            = LiteralArgumentBuilder.literal(CompactMachines.MOD_ID);

    public static void prepare() {

    }

    public static LiteralArgumentBuilder<CommandSourceStack> getRoot() {
        return CM_COMMAND_ROOT;
    }

    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        Commands.CM_COMMAND_ROOT.then(CMTeleportSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMEjectSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMRebindSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMUnbindSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMRoomsSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(CMGiveMachineSubcommand.make());
//        Commands.CM_COMMAND_ROOT.then(SpawnSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(RoomUpgradesSubcommand.make());
        Commands.CM_COMMAND_ROOT.then(EnableBasicTemplatesSubcommand.make());

        event.getDispatcher().register(Commands.CM_COMMAND_ROOT);
    }
}
