package dev.compactmods.machines.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.data.graph.CompactMachineConnectionGraph;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

public class CMMachineDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeMachineCsv() {
        return Commands.literal("machines")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMMachineDataExportCommand::execAll);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

        final var connections = MachineConnections.get(serv);
        final var machines = CompactMachineData.get(serv);

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve("machines.csv")
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeCsv(writer);

            machines.stream().forEach(room -> writeMachine(connections.graph, room, builder));

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.message(Messages.FAILED_CMD_FILE_ERROR));
            return -1;
        }

        return 0;
    }

    @NotNull
    private static CsvOutput makeCsv(BufferedWriter writer) throws IOException {
        return CsvOutput.builder()
                .addColumn("id")
                .addColumn("dim")
                .addColumn("machine_x")
                .addColumn("machine_y")
                .addColumn("machine_z")
                .addColumn("room_x")
                .addColumn("room_z")
                .build(writer);
    }

    private static void writeMachine(CompactMachineConnectionGraph graph, CompactMachineData.MachineData mach, CsvOutput builder) {
        try {
            int id = mach.getMachineId();
            var loc = mach.getLocation();
            var placedAt = loc.getBlockPosition();

            var room = graph.getConnectedRoom(id).orElse(new ChunkPos(-1, -1));
            builder.writeRow(
                    id,
                    loc.getDimension().location().toString(),
                    placedAt.getX(),
                    placedAt.getY(),
                    placedAt.getZ(),
                    room.x,
                    room.z
            );
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
        }
    }
}
