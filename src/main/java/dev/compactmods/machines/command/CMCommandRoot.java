package dev.compactmods.machines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.command.data.CMDataSubcommand;
import dev.compactmods.machines.command.subcommand.*;
import dev.compactmods.machines.upgrade.command.CMUpgradeRoomCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class CMCommandRoot {

    // TODO: /cm create <size:RoomSize> <owner:Player> <giveMachine:true|false>
    // TODO: /cm spawn set <room> <pos>

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(CompactMachines.MOD_ID);
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
}
