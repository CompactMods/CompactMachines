package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.command.CMCommands;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.config.ServerConfig;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
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
        final var source = ctx.getSource();

        if (compactDim == null) {
            source.sendFailure(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }

        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

        if (!(level.getBlockEntity(rebindingMachine) instanceof BoundCompactMachineBlockEntity machine)) {
            LoggingUtil.modLog().error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            source.sendFailure(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
            return -1;
        }

        machine.disconnect();
        return 0;
    }
}
