package dev.compactmods.machines.forge.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.server.level.ServerLevel;

public class CMRebindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("rebind")
                .requires(cs -> cs.hasPermission(ServerConfig.rebindLevel()));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("bindTo", StringArgumentType.string())
                .executes(CMRebindSubcommand::doRebind)));

        return subRoot;
    }

    private static int doRebind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var server = ctx.getSource().getServer();
        final var level = ctx.getSource().getLevel();
        final ServerLevel compactDim;
        try {
            compactDim = CompactDimension.forServer(server);
        } catch (MissingDimensionException e) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }

        final var roomProvider = CompactRoomProvider.instance(compactDim);
        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomCode = StringArgumentType.getString(ctx, "bindTo");
        roomProvider.forRoom(roomCode).ifPresentOrElse(targetRoom -> {
            CompactMachines.LOGGER.debug("Binding machine at {} to room {}", rebindingMachine, targetRoom.code());

            if(!(level.getBlockEntity(rebindingMachine) instanceof CompactMachineBlockEntity machine)) {
                CompactMachines.LOGGER.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
                throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
            }

            machine.connectedRoom().ifPresentOrElse(currentRoom -> {
                final var currentRoomTunnels = TunnelConnectionGraph.forRoom(compactDim, currentRoom);
                final var firstTunnel = currentRoomTunnels.positions(machine.getLevelPosition()).findFirst();
                firstTunnel.ifPresent(ft -> {
                    throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NO_REBIND_TUNNEL_PRESENT, ft));
                });

                // No tunnels - clear to rebind
                machine.setConnectedRoom(targetRoom);
            }, () -> machine.setConnectedRoom(targetRoom));
        }, () -> {
            CompactMachines.LOGGER.error("Cannot rebind to room {}; not registered.", roomCode);
        });

        return 0;
    }
}
