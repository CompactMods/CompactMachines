package dev.compactmods.machines.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CMRoomDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeRoomCsv() {
        return Commands.literal("rooms")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMRoomDataExportCommand::execAll);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();
        var compact = src.getServer().getLevel(Registration.COMPACT_DIMENSION);

        final CompactRoomData rooms = CompactRoomData.get(serv);

        var outdir = src.getServer().getFile(CompactMachines.MOD_ID);
        var out = outdir.toPath()
                .resolve("rooms.csv")
                .toAbsolutePath();

        try {
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeCsv(writer);

            rooms.streamRooms().forEach(room -> writeRoom(room, builder));

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
                .addColumn("room_x")
                .addColumn("room_z")
                .addColumn("size")
                .addColumn("owner_uuid")
                .addColumn("spawn_x")
                .addColumn("spawn_y")
                .addColumn("spawn_z")
                .build(writer);
    }

    private static void writeRoom(CompactRoomData.RoomData room, CsvOutput builder) {
        try {
            ChunkPos chunk = new ChunkPos(room.getCenter());
            final Vec3 spawn = room.getSpawn();

            builder.writeRow(
                    chunk.x, chunk.z,
                    room.getSize().getSerializedName(),
                    room.getOwner().toString(),
                    spawn.x, spawn.y, spawn.z
            );
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
        }
    }
}
