package dev.compactmods.machines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class CMRebindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("rebind")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("bindTo", StringArgumentType.string())
                        .executes(CMRebindSubcommand::doRebind)));

        return subRoot;
    }

    private static int doRebind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var level = ctx.getSource().getLevel();
        final var source = ctx.getSource();

        final var LOGS = LoggingUtil.modLog();

        final var roomProvider = CompactMachines.roomRegistrar(source.getServer());
        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomCode = StringArgumentType.getString(ctx, "bindTo");

        if (!(level.getBlockEntity(rebindingMachine) instanceof BoundCompactMachineBlockEntity machine)) {
            LOGS.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            source.sendFailure(MachineTranslations.NOT_A_MACHINE_BLOCK.apply(rebindingMachine));
            return -1;
        }

        roomProvider.get(roomCode).ifPresentOrElse(targetRoom -> {
            LOGS.info("Binding machine at {} to room {}", rebindingMachine, roomCode);
            machine.setConnectedRoom(roomCode);
        }, () -> {
            LOGS.error("Cannot rebind to room {}; not registered.", roomCode);
        });

        return 0;
    }
}
