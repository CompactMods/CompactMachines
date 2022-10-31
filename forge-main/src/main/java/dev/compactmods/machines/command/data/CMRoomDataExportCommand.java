package dev.compactmods.machines.command.data;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.CMCommands;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomSpawnLookup;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Vec3i;
import net.minecraft.util.CsvOutput;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class CMRoomDataExportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> makeRoomCsv() {
        return Commands.literal("rooms")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMRoomDataExportCommand::execAll);
    }

    private static int execAll(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        try {
            var roomProvider = CompactRoomProvider.instance(src.getServer());

            var outdir = src.getServer().getFile(Constants.MOD_ID);
            var out = outdir.toPath()
                    .resolve("rooms.csv")
                    .toAbsolutePath();
            Files.createDirectories(outdir.toPath());

            var writer = Files.newBufferedWriter(out);
            CsvOutput builder = makeCsv(writer);

            roomProvider.allRooms().forEach(room -> writeRoom(builder, room, roomProvider, roomProvider));

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
                .addColumn("dev/compactmods/machines/api/room")
                .addColumn("owner_uuid")
                .addColumn("size_x")
                .addColumn("size_y")
                .addColumn("size_z")
                .addColumn("spawn_x")
                .addColumn("spawn_y")
                .addColumn("spawn_z")
                .build(writer);
    }

    private static void writeRoom(CsvOutput builder, IRoomRegistration room, IRoomOwnerLookup owners, IRoomSpawnLookup spawns) {
        try {
            final Vec3i roomDims = room.dimensions();
            final Vec3 spawn = room.spawnPosition(spawns);

            builder.writeRow(
                    room.code(),
                    room.owner(owners),
                    roomDims.getX(), roomDims.getY(), roomDims.getZ(),
                    spawn.x, spawn.y, spawn.z
            );
        } catch (IOException e) {
            CompactMachines.LOGGER.error(e);
        }
    }
}
