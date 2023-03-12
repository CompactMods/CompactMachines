package dev.compactmods.machines.forge.command.data;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CsvOutput;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CMTunnelDataExportCommand {

    private static final Logger LOGGER = LoggingUtil.modLog();

    public static ArgumentBuilder<CommandSourceStack, ?> makeTunnelCsv() {
        var chunk = Commands
                .argument("room", StringArgumentType.string())
                .executes(CMTunnelDataExportCommand::exec);

        return Commands.literal("tunnels")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(CMTunnelDataExportCommand::execAll)
                .then(chunk);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();
        try (var compactDim = CompactDimension.forServer(serv)) {
            final var roomProvider = CompactRoomProvider.instance(compactDim);

            var outdir = src.getServer().getFile(Constants.MOD_ID);
            var out = outdir.toPath()
                    .resolve("tunnels.csv")
                    .toAbsolutePath();

            try {
                Files.createDirectories(outdir.toPath());

                var writer = Files.newBufferedWriter(out);
                CsvOutput builder = makeTunnelCsvOut(writer);

                roomProvider.allRooms().forEach(room -> {
                    try {
                        writeRoomTunnels(compactDim, room.code(), builder);
                    } catch (MissingDimensionException e) {
                        LOGGER.error(e);
                    }
                });

                writer.close();
            } catch (IOException e) {
                LOGGER.error(e);
                src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
                return -1;
            }
        } catch (IOException | MissingDimensionException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }

    public static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();

        final var room = StringArgumentType.getString(ctx, "room");
        final var compactDim = src.getServer().getLevel(CompactDimension.LEVEL_KEY);

        var outdir = src.getServer().getFile(Constants.MOD_ID);
        var out = outdir.toPath()
                .resolve(String.format("tunnels_%s.csv", room))
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeTunnelCsvOut(writer);
            writeRoomTunnels(compactDim, room, builder);

            writer.close();
        } catch (IOException e) {
            LOGGER.error(e);
            src.sendFailure(TranslationUtil.command(CMCommands.FAILED_CMD_FILE_ERROR));
            return -1;
        } catch (MissingDimensionException e) {
            LOGGER.error(e);
        }

        return 0;
    }

    @NotNull
    private static CsvOutput makeTunnelCsvOut(BufferedWriter writer) throws IOException {
        return CsvOutput.builder()
                .addColumn("type")
                .addColumn("side")
                .addColumn("pos_x").addColumn("pos_y").addColumn("pos_z")
                .addColumn("machine_id")
                .build(writer);
    }

    private static void writeRoomTunnels(ServerLevel compactDim, String roomCode, CsvOutput builder) throws MissingDimensionException {
        final var graph = TunnelConnectionGraph.forRoom(compactDim, roomCode);
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
                LOGGER.warn("Error writing tunnel record.", e);
            }
        });
    }
}
