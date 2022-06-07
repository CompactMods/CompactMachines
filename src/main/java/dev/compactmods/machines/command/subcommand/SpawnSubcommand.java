package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SpawnSubcommand {

    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var spawnRoot = Commands.literal("spawn");

        final var resetSpawn = Commands.literal("reset")
                .requires(cs -> cs.hasPermission(ServerConfig.changeRoomSpawn()))
                .then(Commands.argument("room", RoomPositionArgument.room())
                .executes(SpawnSubcommand::resetRoomSpawn));

        spawnRoot.then(resetSpawn);

        return spawnRoot;
    }

    private static int resetRoomSpawn(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();
        final var serv = src.getServer();
        final var roomPos = RoomPositionArgument.get(ctx, "room");

        try {
            Rooms.resetSpawn(serv, roomPos);
            src.sendSuccess(TranslationUtil.command(CMCommands.SPAWN_CHANGED_SUCCESSFULLY, "%s, %s".formatted(roomPos.x, roomPos.z)), true);
            return 0;
        } catch (NonexistentRoomException e) {
            src.sendFailure(TranslationUtil.command(CMCommands.ROOM_NOT_FOUND, "%s, %s".formatted(roomPos.x, roomPos.z)));
            return -1;
        }
    }

}
