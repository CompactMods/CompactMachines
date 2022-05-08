package dev.compactmods.machines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.command.data.CMDataSubcommand;
import dev.compactmods.machines.command.subcommand.*;
import net.minecraft.commands.CommandSourceStack;

public class CMCommandRoot {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(CompactMachines.MOD_ID);
        root.then(CMEjectSubcommand.make());
        root.then(CMSummarySubcommand.make());
        root.then(CMRebindSubcommand.make());
        root.then(CMReaddDimensionSubcommand.make());
        root.then(CMRoomsSubcommand.make());
        root.then(CMDataSubcommand.make());
        root.then(CMGiveMachineSubcommand.make());

        dispatcher.register(root);
    }
}
