package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.CMCommands;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SpawnSubcommand {

    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var spawnRoot = Commands.literal("spawn");

        final var resetSpawn = Commands.literal("reset")
                .requires(cs -> cs.hasPermission(ServerConfig.changeRoomSpawn()))
                .then(Commands.argument("dev/compactmods/machines/api/room", StringArgumentType.string())
                        .executes(SpawnSubcommand::resetRoomSpawn));

        spawnRoot.then(resetSpawn);

        return spawnRoot;
    }

    private static int resetRoomSpawn(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();
        final var serv = src.getServer();
        final var roomCode = StringArgumentType.getString(ctx, "dev/compactmods/machines/api/room");

        try {
            final var compactDim = CompactDimension.forServer(serv);
            final var roomProvider = CompactRoomProvider.instance(compactDim);

            roomProvider.resetSpawn(roomCode);
            src.sendSuccess(TranslationUtil.command(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "%s".formatted(roomCode)), true);
            return 0;
        } catch (MissingDimensionException e) {
            src.sendFailure(TranslationUtil.command(Messages.UNREGISTERED_CM_DIM));
            return -1;
        } catch (NonexistentRoomException e) {
            src.sendFailure(TranslationUtil.command(CMCommands.ROOM_NOT_FOUND, roomCode));
            return -1;
        }
    }
}
