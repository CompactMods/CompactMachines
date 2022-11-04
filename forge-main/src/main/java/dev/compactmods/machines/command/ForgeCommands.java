package dev.compactmods.machines.command;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.data.CMDataSubcommand;
import dev.compactmods.machines.command.subcommand.CMEjectSubcommand;
import dev.compactmods.machines.command.subcommand.CMGiveMachineSubcommand;
import dev.compactmods.machines.command.subcommand.CMReaddDimensionSubcommand;
import dev.compactmods.machines.command.subcommand.CMRebindSubcommand;
import dev.compactmods.machines.command.subcommand.CMRoomUpgradeCommand;
import dev.compactmods.machines.command.subcommand.CMRoomsSubcommand;
import dev.compactmods.machines.command.subcommand.CMSummarySubcommand;
import dev.compactmods.machines.command.subcommand.CMUnbindSubcommand;
import dev.compactmods.machines.command.subcommand.SpawnSubcommand;
import dev.compactmods.machines.Registries;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static dev.compactmods.machines.command.Commands.CM_COMMAND_ROOT;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeCommands {

    static {
        Registries.COMMAND_ARGUMENT_TYPES.register("room_upgrade",
                () -> ArgumentTypeInfos.registerByClass(RoomUpgradeArgument.class, SingletonArgumentInfo.contextFree(RoomUpgradeArgument::upgrade)));

        CM_COMMAND_ROOT.then(CMEjectSubcommand.make());
        CM_COMMAND_ROOT.then(CMSummarySubcommand.make());
        CM_COMMAND_ROOT.then(CMRebindSubcommand.make());
        CM_COMMAND_ROOT.then(CMUnbindSubcommand.make());
        CM_COMMAND_ROOT.then(CMReaddDimensionSubcommand.make());
        CM_COMMAND_ROOT.then(CMRoomsSubcommand.make());
        CM_COMMAND_ROOT.then(CMDataSubcommand.make());
        CM_COMMAND_ROOT.then(CMGiveMachineSubcommand.make());
        CM_COMMAND_ROOT.then(SpawnSubcommand.make());
        CM_COMMAND_ROOT.then(CMRoomUpgradeCommand.make());
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.getRoot());
    }
}
