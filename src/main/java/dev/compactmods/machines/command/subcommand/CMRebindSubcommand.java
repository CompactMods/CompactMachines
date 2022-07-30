package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class CMRebindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("rebind")
                .requires(cs -> cs.hasPermission(ServerConfig.rebindLevel()));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("bindTo", RoomPositionArgument.room())
                .executes(CMRebindSubcommand::doRebind)));

        return subRoot;
    }

    private static int doRebind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var server = ctx.getSource().getServer();
        final var level = ctx.getSource().getLevel();
        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);
        if(compactDim == null) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }

        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomPos = RoomPositionArgument.get(ctx, "bindTo");

        CompactMachines.LOGGER.debug("Binding machine at {} to room chunk {}", rebindingMachine, roomPos);

        if(!(level.getBlockEntity(rebindingMachine) instanceof CompactMachineBlockEntity machine)) {
            CompactMachines.LOGGER.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
        }

        machine.getConnectedRoom().ifPresentOrElse(currentRoom -> {
            final var currentRoomTunnels = TunnelConnectionGraph.forRoom(compactDim, currentRoom);
            final var firstTunnel = currentRoomTunnels.getConnections(machine.getLevelPosition()).findFirst();
            firstTunnel.ifPresent(ft -> {
                throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NO_REBIND_TUNNEL_PRESENT, ft));
            });

            // No tunnels - clear to rebind
            machine.setConnectedRoom(roomPos);
        }, () -> machine.setConnectedRoom(roomPos));

        return 0;
    }
}
