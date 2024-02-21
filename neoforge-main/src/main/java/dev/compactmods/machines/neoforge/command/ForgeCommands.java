package dev.compactmods.machines.neoforge.command;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.neoforge.command.subcommand.CMEjectSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMGiveMachineSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMRebindSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMRoomsSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMSummarySubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMTeleportSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.CMUnbindSubcommand;
import dev.compactmods.machines.neoforge.command.subcommand.SpawnSubcommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeCommands {

    static {
        // FIXME
//        Registries.COMMAND_ARGUMENT_TYPES.register("room_upgrade",
//                () -> ArgumentTypeInfos.registerByClass(RoomUpgradeArgument.class, SingletonArgumentInfo.contextFree(RoomUpgradeArgument::upgrade)));

        var cm = Commands.getRoot();

        cm.then(CMTeleportSubcommand.make());
        cm.then(CMEjectSubcommand.make());
        cm.then(CMSummarySubcommand.make());
        cm.then(CMRebindSubcommand.make());
        cm.then(CMUnbindSubcommand.make());
        cm.then(CMRoomsSubcommand.make());
        cm.then(CMGiveMachineSubcommand.make());
        cm.then(SpawnSubcommand.make());
//        cm.then(CMRoomUpgradeCommand.make());
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.getRoot());
    }
}
