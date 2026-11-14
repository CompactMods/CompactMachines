package dev.compactmods.machines.command;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.command.CMRoomsSubcommand;
import dev.compactmods.machines.room.command.CMRoomCoreSubcommand;
import dev.compactmods.machines.room.command.EnableBasicTemplatesSubcommand;
import dev.compactmods.machines.room.command.SpawnSubcommand;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Commands {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    public static void prepare() {

    }

    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        CompactMachinesCore.CM_COMMAND_ROOT.then(CMTeleportSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(CMEjectSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(CMRoomsSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(CMRoomCoreSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(SpawnSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(dev.compactmods.machines.upgrades.command.RoomUpgradesSubcommand.make());
        CompactMachinesCore.CM_COMMAND_ROOT.then(EnableBasicTemplatesSubcommand.make());

        event.getDispatcher().register(CompactMachinesCore.CM_COMMAND_ROOT);
    }
}
