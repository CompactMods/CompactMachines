package dev.compactmods.machines.command.subcommand;

import java.util.Collection;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class CMEjectSubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("eject")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMEjectSubcommand::execExecutingPlayer)
                    .then(Commands.argument("player", EntityArgument.player())
                    .executes(CMEjectSubcommand::execSpecificPlayer));
    }

    private static int execSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> ent = EntityArgument.getPlayers(ctx, "player");
        ent.forEach(player -> {
            player.getCapability(Capabilities.ROOM_HISTORY).ifPresent(IRoomHistory::clear);
            PlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);
        });

        return 0;
    }

    private static int execExecutingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerPlayer player = ctx.getSource().getPlayerOrException();

        player.getCapability(Capabilities.ROOM_HISTORY).ifPresent(IRoomHistory::clear);
        PlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);

        return 0;
    }
}
