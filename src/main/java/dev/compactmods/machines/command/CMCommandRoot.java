package dev.compactmods.machines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.commands.CommandSourceStack;

public class CMCommandRoot {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal(CompactMachines.MOD_ID);
        root.then(CMEjectSubcommand.register());
        root.then(CMSummarySubcommand.register());
        dispatcher.register(root);
    }
}
