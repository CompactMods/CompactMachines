package dev.compactmods.machines.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.data.CompactRoomData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CMTunnelDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeTunnelCsv() {
        var chunk = Commands
                .argument("chunkx", IntegerArgumentType.integer())
                .then(Commands.argument("chunkz", IntegerArgumentType.integer()))
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
                var chunk1 = compact.getChunk(roomChunk.x, roomChunk.z);
                writeRoomTunnels(chunk1, builder);
            });

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.message(Messages.FAILED_CMD_FILE_ERROR));
            return -1;
        }

        return 0;
    }

    public static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        ServerPlayer player = src.getPlayerOrException();

        final int chunkx = IntegerArgumentType.getInteger(ctx, "chunkx");
        final int chunkz = IntegerArgumentType.getInteger(ctx, "chunkz");

        var chunk1 = src.getLevel().getChunk(chunkx, chunkz);

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve(String.format("tunnels_%s_%s.csv", chunkx, chunkz))
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeTunnelCsvOut(writer);
            writeRoomTunnels(chunk1, builder);

            writer.close();
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
            src.sendFailure(TranslationUtil.message(Messages.FAILED_CMD_FILE_ERROR));
            return -1;
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

    private static void writeRoomTunnels(LevelChunk chunk1, CsvOutput builder) {
        // TODO Reimplement
//        chunk1.getCapability(Capabilities.ROOM_TUNNELS).ifPresent(tunnels -> {
//            tunnels.streamLocations().forEach(pos -> {
//                tunnels.locatedAt(pos).ifPresent(conn -> {
//                    try {
//                        if(chunk1.getBlockEntity(pos) instanceof TunnelWallEntity tun) {
//                            builder.writeRow(
//                                    conn.type().getRegistryName().toString(),
//                                    conn.side().getSerializedName(),
//                                    pos.getX(), pos.getY(), pos.getZ(),
//                                    tun.getMachine()
//                            );
//                        }
//                    } catch (IOException e) {
//                        CompactMachines.LOGGER.error(e);
//                    }
//                });
//            });
//
//        });
    }
}
