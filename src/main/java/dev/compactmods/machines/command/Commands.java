package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.command.data.CMDataSubcommand;
import dev.compactmods.machines.command.subcommand.CMEjectSubcommand;
import dev.compactmods.machines.command.subcommand.CMGiveMachineSubcommand;
import dev.compactmods.machines.command.subcommand.CMReaddDimensionSubcommand;
import dev.compactmods.machines.command.subcommand.CMRebindSubcommand;
import dev.compactmods.machines.command.subcommand.CMRoomsSubcommand;
import dev.compactmods.machines.command.subcommand.CMSummarySubcommand;
import dev.compactmods.machines.command.subcommand.CMUnbindSubcommand;
import dev.compactmods.machines.command.subcommand.SpawnSubcommand;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.upgrade.command.CMUpgradeRoomCommand;
import dev.compactmods.machines.upgrade.command.RoomUpgradeArgument;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class Commands {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    static {
        Registries.COMMAND_ARGUMENT_TYPES.register("room_upgrade",
                () -> ArgumentTypeInfos.registerByClass(RoomUpgradeArgument.class, SingletonArgumentInfo.contextFree(RoomUpgradeArgument::upgrade)));
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(Constants.MOD_ID);
        root.then(CMEjectSubcommand.make());
        root.then(CMSummarySubcommand.make());
        root.then(CMRebindSubcommand.make());
        root.then(CMUnbindSubcommand.make());
        root.then(CMReaddDimensionSubcommand.make());
        root.then(CMRoomsSubcommand.make());
        root.then(CMDataSubcommand.make());
        root.then(CMGiveMachineSubcommand.make());
        root.then(SpawnSubcommand.make());
        root.then(CMUpgradeRoomCommand.make());

        event.getDispatcher().register(root);
    }

    public static void prepare() {

    }
}
