package dev.compactmods.machines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.i18n.RoomTranslations;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.command.argument.Suggestors;
import dev.compactmods.machines.room.RoomHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;

public class CMTeleportSubcommand {

    private static final Logger LOGGER = LoggingUtil.modLog();

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("tp")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

        subRoot.then(Commands.argument("room", StringArgumentType.string())
                .suggests(Suggestors.ROOM_CODES)
                .executes(CMTeleportSubcommand::teleportExecutor));

        subRoot.then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_CODES)
                        .executes(CMTeleportSubcommand::teleportSpecificPlayer)));

        return subRoot;
    }

    private static void teleportToRoom(CommandSourceStack src, MinecraftServer server, ServerPlayer player, String roomCode) {
        CompactMachines.room(roomCode).ifPresentOrElse(room -> {
            try {
                RoomHelper.teleportPlayerIntoRoom(server, player, room, RoomEntryPoint.playerUsingCommand(player));
            } catch (MissingDimensionException e) {
                // TODO LOGS
            }
        }, () -> {
            LOGGER.error("Error teleporting player into room: room not found.");
            src.sendFailure(RoomTranslations.UNKNOWN_ROOM_BY_CODE.apply(roomCode));
        });
    }

    private static int teleportExecutor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var server = src.getServer();
        final var player = src.getPlayerOrException();
        final var roomCode = StringArgumentType.getString(ctx, "room");

        teleportToRoom(src, server, player, roomCode);
        return 0;
    }

    private static int teleportSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var server = src.getServer();
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var roomCode = StringArgumentType.getString(ctx, "room");

        teleportToRoom(src, server, player, roomCode);
        return 0;
    }
}
