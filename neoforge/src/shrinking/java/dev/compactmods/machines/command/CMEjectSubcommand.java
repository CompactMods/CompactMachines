package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
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
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .executes(CMEjectSubcommand::execSpecificPlayer));
    }

    private static int execSpecificPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Collection<ServerPlayer> ent = EntityArgument.getPlayers(ctx, "player");

        ent.forEach(player -> player.getExistingData(Shrinking.CURRENT_ROOM_CODE).ifPresent(_ -> {
            try (final var history = player.getCapability(Shrinking.HISTORY_MANAGER)) {
                if (history != null)
                    history.clear();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            ShrinkingHelper.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);
        }));

        return 0;
    }

    private static int execExecutingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final ServerPlayer player = ctx.getSource().getPlayerOrException();
        final MinecraftServer server = ctx.getSource().getServer();

        server.submitAsync(() -> {
            final var history = player.getCapability(Shrinking.HISTORY_MANAGER);
            if (history != null)
                history.clear();
        });

        ShrinkingHelper.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);
        return 0;
    }
}
