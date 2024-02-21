package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.command.CMCommands;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

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
        final var level = ctx.getSource().getLevel();
        final var source = ctx.getSource();

        final var LOGS = LoggingUtil.modLog();

        final var roomProvider = RoomApi.registrar();
        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomCode = StringArgumentType.getString(ctx, "bindTo");
        roomProvider.get(roomCode).ifPresentOrElse(targetRoom -> {
            LOGS.debug("Binding machine at {} to room {}", rebindingMachine, roomCode);

            if (!(level.getBlockEntity(rebindingMachine) instanceof BoundCompactMachineBlockEntity machine)) {
                LOGS.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
                source.sendFailure(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
                return;
            }

            // No tunnels - clear to rebind
            machine.setConnectedRoom(roomCode);
        }, () -> {
            LOGS.error("Cannot rebind to room {}; not registered.", roomCode);
        });

        return 0;
    }
}
