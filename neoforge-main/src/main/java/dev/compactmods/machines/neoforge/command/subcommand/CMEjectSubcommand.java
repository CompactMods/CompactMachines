package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import dev.compactmods.machines.player.PlayerEntryPointHistory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class CMEjectSubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("eject")
                .executes(CMEjectSubcommand::execExecutingPlayer)
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(CMEjectSubcommand::execSpecificPlayer));
    }

    private static int execSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> ent = EntityArgument.getPlayers(ctx, "player");
        ent.forEach(player -> {
            // FIXME History player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(IRoomHistory::clear);
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);
        });

        return 0;
    }

    private static int execExecutingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerPlayer player = ctx.getSource().getPlayerOrException();
        final MinecraftServer server = ctx.getSource().getServer();

        server.submitAsync(() -> {
            try {
                PlayerEntryPointHistory.forServer(server).clearHistory(player);
            } catch (MissingDimensionException e) {
                throw new RuntimeException(e);
            }
        });

        ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);

        return 0;
    }
}
