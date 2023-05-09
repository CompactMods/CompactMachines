package dev.compactmods.machines.forge.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.forge.command.argument.Suggestors;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
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
                .requires(cs -> cs.hasPermission(ServerConfig.giveMachineLevel()));

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
        var roomProvider = CompactRoomProvider.instance(src.getServer());
        roomProvider.forRoom(roomCode).ifPresentOrElse(room -> {
            try {
                RoomHelper.teleportPlayerIntoRoom(server, player, room);
            } catch (MissingDimensionException | NonexistentRoomException e) {
                throw new RuntimeException(e);
            }
        }, () -> {
            LOGGER.error("Error teleporting player into room: room not found.");
            src.sendFailure(TranslationUtil.message(Messages.UNKNOWN_ROOM_CHUNK, roomCode));
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
