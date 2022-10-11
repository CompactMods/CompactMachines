package dev.compactmods.machines.command.data;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CMDataSubcommand {
    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal("data");

        var export = Commands.literal("export");
        export.then(CMMachineDataExportCommand.makeMachineCsv());
        export.then(CMTunnelDataExportCommand.makeTunnelCsv());
        export.then(CMRoomDataExportCommand.makeRoomCsv());
        root.then(export);

        return root;
    }


}
