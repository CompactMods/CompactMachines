package dev.compactmods.machines.forge.command;

import dev.compactmods.machines.forge.Registries;
import dev.compactmods.machines.forge.command.data.CMDataSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMEjectSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMGiveMachineSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMReaddDimensionSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMRebindSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMRoomUpgradeCommand;
import dev.compactmods.machines.forge.command.subcommand.CMRoomsSubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMSummarySubcommand;
import dev.compactmods.machines.forge.command.subcommand.CMUnbindSubcommand;
import dev.compactmods.machines.forge.command.subcommand.SpawnSubcommand;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class ForgeCommands {

    static {
        Registries.COMMAND_ARGUMENT_TYPES.register("room_upgrade",
                () -> ArgumentTypeInfos.registerByClass(RoomUpgradeArgument.class, SingletonArgumentInfo.contextFree(RoomUpgradeArgument::upgrade)));

        var cm = Commands.getRoot();

        cm.then(CMEjectSubcommand.make());
        cm.then(CMSummarySubcommand.make());
        cm.then(CMRebindSubcommand.make());
        cm.then(CMUnbindSubcommand.make());
        cm.then(CMReaddDimensionSubcommand.make());
        cm.then(CMRoomsSubcommand.make());
        cm.then(CMDataSubcommand.make());
        cm.then(CMGiveMachineSubcommand.make());
        cm.then(SpawnSubcommand.make());
        cm.then(CMRoomUpgradeCommand.make());
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.getRoot());
    }
}
