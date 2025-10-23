package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.world.level.block.Block;

public class CMUnbindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("unbind")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(CMUnbindSubcommand::doUnbind));

        return subRoot;
    }

    private static int doUnbind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var server = ctx.getSource().getServer();
        final var level = ctx.getSource().getLevel();
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var source = ctx.getSource();

        if (compactDim == null) return -1;

        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

        if (!(level.getBlockEntity(rebindingMachine) instanceof BoundCompactMachineBlockEntity)) {
            LoggingUtil.modLog().error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            source.sendFailure(MachineTranslations.NOT_A_MACHINE_BLOCK.apply(rebindingMachine));
            return -1;
        }

        level.setBlock(rebindingMachine, Machines.Blocks.UNBOUND_MACHINE.get().defaultBlockState(), Block.UPDATE_ALL);
        return 0;
    }
}
