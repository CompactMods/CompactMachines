package dev.compactmods.machines.command.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.CsvOutput;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CMMachineDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeMachineCsv() {
        return Commands.literal("machines")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMMachineDataExportCommand::execAll);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

        var outdir = src.getServer().getFile(Constants.MOD_ID);
        var out = outdir.toPath()
                .resolve("machines.csv")
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeCsv(writer);

            for (final var dim : serv.getAllLevels()) {
                final var graph = DimensionMachineGraph.forDimension(dim);
                graph.getMachines().forEach(machNode -> {
                    final var m = machNode.dimpos();
                    final var r = graph.getConnectedRoom(m.getBlockPosition());
                    r.ifPresent(room -> writeMachine(m, room, builder));
                });
            }

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
            return -1;
        }

        return 0;
    }

    @Nonnull
    private static CsvOutput makeCsv(BufferedWriter writer) throws IOException {
        return CsvOutput.builder()
                .addColumn("dim")
                .addColumn("machine_x")
                .addColumn("machine_y")
                .addColumn("machine_z")
                .addColumn("room")
                .build(writer);
    }

    private static void writeMachine(IDimensionalBlockPosition machine, String room, CsvOutput builder) {
        try {
            var placedAt = machine.getBlockPosition();

            builder.writeRow(
                    machine.dimension().location().toString(),
                    placedAt.getX(),
                    placedAt.getY(),
                    placedAt.getZ(),
                    room
            );
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
        }
    }
}
