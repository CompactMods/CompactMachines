package dev.compactmods.machines.command.data;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Column;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CMTunnelDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeTunnelCsv() {
        var chunk = Commands
                .argument("room", RoomPositionArgument.room())
                .executes(CMTunnelDataExportCommand::exec);

        return Commands.literal("tunnels")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(CMTunnelDataExportCommand::execAll)
                .then(chunk);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();
        var compact = src.getServer().getLevel(Registration.COMPACT_DIMENSION);

        final CompactRoomData rooms;
        try {
            rooms = CompactRoomData.get(serv);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return -1;
        }

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve("tunnels.csv")
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeTunnelCsvOut(writer);

            rooms.stream().forEach(roomChunk -> {
                try {
                    writeRoomTunnels(serv, roomChunk, builder);
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.error(e);
                }
            });

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
            return -1;
        }

        return 0;
    }

    public static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();

        final var room = RoomPositionArgument.get(ctx, "room");

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve(String.format("tunnels_%s_%s.csv", room.x, room.z))
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeTunnelCsvOut(writer);
            writeRoomTunnels(src.getServer(), room, builder);

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
            return -1;
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.error(e);
        }

        return 0;
    }

    @Nonnull
    private static CsvOutput makeTunnelCsvOut(BufferedWriter writer) throws IOException {
        return CsvOutput.builder()
                .addColumn("type")
                .addColumn("side")
                .addColumn("pos_x").addColumn("pos_y").addColumn("pos_z")
                .addColumn("machine_id")
                .build(writer);
    }

    private static void writeRoomTunnels(MinecraftServer server, ChunkPos room, CsvOutput builder) throws MissingDimensionException {
        // TODO Reimplement
        final var data = RoomTunnelData.get(server, room);

        final var graph = data.getGraph();
        graph.tunnels().forEach(info -> {
            var pos = info.location();
            try {
                builder.writeRow(
                        info.type().toString(),
                        info.side().getSerializedName(),
                        pos.getX(), pos.getY(), pos.getZ(),
                        info.machine()
                );
            } catch (IOException e) {
                CompactMachines.LOGGER.warn("Error writing tunnel record.", e);
            }
        });
    }
}
