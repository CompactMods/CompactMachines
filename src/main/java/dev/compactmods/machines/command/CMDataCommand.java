package dev.compactmods.machines.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CMDataCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal("cmdata");

        var export = Commands.literal("export");
        export.then(CMMachineDataExportCommand.makeMachineCsv());
        export.then(CMTunnelDataExportCommand.makeTunnelCsv());
        export.then(CMRoomDataExportCommand.makeRoomCsv());
        root.then(export);

        // root.then(CMFixBiomeSubcommand.register());
        dispatcher.register(root);
    }


}
