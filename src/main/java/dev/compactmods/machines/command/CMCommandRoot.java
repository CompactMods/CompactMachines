package dev.compactmods.machines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.command.CommandSource;

public class CMCommandRoot {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        final LiteralArgumentBuilder<CommandSource> root = LiteralArgumentBuilder.literal(CompactMachines.MOD_ID);
        root.then(CMEjectSubcommand.register());
        root.then(CMFixBiomeSubcommand.register());
        dispatcher.register(root);
    }
}
