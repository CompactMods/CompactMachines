package dev.compactmods.machines.command.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.CsvOutput;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;

// TODO
public class CMMachineDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeMachineCsv() {
        return Commands.literal("machines")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMMachineDataExportCommand::execAll);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

//        final CompactMachineGraph machines;
//        final MachineToRoomConnections connections;
//        try {
//            machines = CompactMachineGraph.forDimension(serv);
//            connections = MachineToRoomConnections.get(serv);
//        } catch (MissingDimensionException e) {
//            CompactMachines.LOGGER.fatal(e);
//            return -1;
//        }
//
//        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
//        var out = outdir.toPath()
//                .resolve("machines.csv")
//                .toAbsolutePath();
//
//        try {
//            Files.createDirectories(outdir.toPath());
//
//            var writer = Files.newBufferedWriter(out);
//            CsvOutput builder = makeCsv(writer);
//
//            machines.getMachines().forEach(node -> writeMachine(connections, node, builder));
//
//            writer.close();
//        } catch (IOException e) {
//            CompactMachines.LOGGER.error(e);
//            src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
//            return -1;
//        }

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

    private static void writeMachine(MachineRoomConnections connections, CsvOutput builder) {
//        try {
//            int id = mach.getMachineId();
//            var loc = mach.getLocation();
//            var placedAt = loc.getBlockPosition();
//
//            var room = connections.getConnectedRoom(id).orElse(new ChunkPos(-1, -1));
//            builder.writeRow(
//                    id,
//                    loc.getDimension().location().toString(),
//                    placedAt.getX(),
//                    placedAt.getY(),
//                    placedAt.getZ(),
//                    room.x,
//                    room.z
//            );
//        } catch (IOException e) {
//            CompactMachines.LOGGER.error(e);
//        }
    }
}
