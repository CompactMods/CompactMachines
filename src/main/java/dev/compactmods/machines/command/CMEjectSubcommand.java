package dev.compactmods.machines.command;

import java.util.Collection;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.rooms.capability.CapabilityRoomHistory;
import dev.compactmods.machines.rooms.capability.IRoomHistory;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CMEjectSubcommand {
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("eject")
                .requires(cs -> cs.hasPermission(2))
                .executes(CMEjectSubcommand::execExecutingPlayer)
                    .then(Commands.argument("player", EntityArgument.player())
                    .executes(CMEjectSubcommand::execSpecificPlayer));
    }

    private static int execSpecificPlayer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        Collection<ServerPlayerEntity> ent = EntityArgument.getPlayers(ctx, "player");
        ent.forEach(player -> {
            player.getCapability(CapabilityRoomHistory.HISTORY_CAPABILITY).ifPresent(IRoomHistory::clear);
            PlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);
        });

        return 0;
    }

    private static int execExecutingPlayer(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        final ServerPlayerEntity player = ctx.getSource().getPlayerOrException();

        player.getCapability(CapabilityRoomHistory.HISTORY_CAPABILITY).ifPresent(IRoomHistory::clear);
        PlayerUtil.teleportPlayerToRespawnOrOverworld(ctx.getSource().getServer(), player);

        return 0;
    }
}
