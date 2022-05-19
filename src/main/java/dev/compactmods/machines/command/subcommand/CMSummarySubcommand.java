package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class CMSummarySubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("summary")
                // .requires(cs -> cs.hasPermission(Commands.LEVEL_ALL))
                .executes(CMSummarySubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        var serv = src.getServer();

        var compactLevel = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactLevel != null) {
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_REGISTERED).withStyle(ChatFormatting.DARK_GREEN), false);
        } else {
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND).withStyle(ChatFormatting.RED), false);
        }

        HashMap<ResourceKey<Level>, Long> levelCounts = new HashMap<>();
        serv.getAllLevels().forEach(sl -> {
            final var machineData = DimensionMachineGraph.forDimension(sl);
            long numRegistered = machineData.getMachines().count();

            if(numRegistered > 0) {
                src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_REG_DIM, sl.dimension().toString(), numRegistered), false);
                levelCounts.put(sl.dimension(), numRegistered);
            }
        });

        long grandTotal = levelCounts.values().stream().reduce(0L, Long::sum);
        src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_REG_TOTAL, grandTotal).withStyle(ChatFormatting.GOLD), false);

        final var roomData = CompactRoomData.get(compactLevel);

        long numRegistered = roomData.stream().count();
        src.sendSuccess(TranslationUtil.command(CMCommands.ROOM_REG_COUNT, numRegistered), false);

        return 0;
    }
}
