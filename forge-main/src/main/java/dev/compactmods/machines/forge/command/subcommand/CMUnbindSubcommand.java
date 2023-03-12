package dev.compactmods.machines.forge.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.config.ServerConfig;
import dev.compactmods.machines.forge.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class CMUnbindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("unbind")
                .requires(cs -> cs.hasPermission(ServerConfig.rebindLevel()));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(CMUnbindSubcommand::doUnbind));

        return subRoot;
    }

    private static int doUnbind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var server = ctx.getSource().getServer();
        final var level = ctx.getSource().getLevel();
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        if (compactDim == null) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }

        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

        if(!(level.getBlockEntity(rebindingMachine) instanceof CompactMachineBlockEntity machine)) {
            CompactMachines.LOGGER.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
        }

        machine.connectedRoom().ifPresentOrElse(currentRoom -> {
            final var currentRoomTunnels = TunnelConnectionGraph.forRoom(compactDim, currentRoom);
            final var firstTunnel = currentRoomTunnels.getConnections(machine.getLevelPosition()).findFirst();
            firstTunnel.ifPresent(ft -> {
                throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NO_REBIND_TUNNEL_PRESENT, ft));
            });

            machine.disconnect();
        }, () -> {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.MACHINE_NOT_BOUND, rebindingMachine.toShortString()));
        });

        return 0;
    }
}
