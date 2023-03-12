package dev.compactmods.machines.forge.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;

import java.util.stream.LongStream;

public class CMSummarySubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("summary")
                .executes(CMSummarySubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

        try {
            ServerLevel compactLevel = CompactDimension.forServer(serv);
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_REGISTERED).withStyle(ChatFormatting.DARK_GREEN), false);

            final var ls = LongStream.builder();
            serv.getAllLevels().forEach(sl -> {
                final var machineData = DimensionMachineGraph.forDimension(sl);
                long numRegistered = machineData.getMachines().count();

                if(numRegistered > 0) {
                    src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_REG_DIM, sl.dimension().location().toString(), numRegistered), false);
                    ls.add(numRegistered);
                }
            });

            long grandTotal = ls.build().sum();
            src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_REG_TOTAL, grandTotal).withStyle(ChatFormatting.GOLD), false);

            final var roomInfo = CompactRoomProvider.instance(compactLevel);
            src.sendSuccess(TranslationUtil.command(CMCommands.ROOM_REG_COUNT, roomInfo.count()), false);
        } catch (MissingDimensionException e) {
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND).withStyle(ChatFormatting.RED), false);
        }


        return 0;
    }
}
