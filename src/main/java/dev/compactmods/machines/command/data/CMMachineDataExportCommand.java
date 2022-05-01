package dev.compactmods.machines.command.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.level.ChunkPos;

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

        final CompactMachineData machines;
        final MachineRoomConnections connections;
        try {
            machines = CompactMachineData.get(serv);
            connections = MachineToRoomConnections.get(serv);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return -1;
        }

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve("machines.csv")
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeCsv(writer);

            machines.stream().forEach(room -> writeMachine(connections, room, builder));

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
                .addColumn("id")
                .addColumn("dim")
                .addColumn("machine_x")
                .addColumn("machine_y")
                .addColumn("machine_z")
                .addColumn("room_x")
                .addColumn("room_z")
                .build(writer);
    }

    private static void writeMachine(MachineRoomConnections connections, CompactMachineData.MachineData mach, CsvOutput builder) {
        try {
            int id = mach.getMachineId();
            var loc = mach.getLocation();
            var placedAt = loc.getBlockPosition();

            var room = connections.getConnectedRoom(id).orElse(new ChunkPos(-1, -1));
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
