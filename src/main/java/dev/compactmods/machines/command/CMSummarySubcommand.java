package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CMSummarySubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("summary")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_ALL))
                .executes(CMSummarySubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        var serv = src.getServer();

        var compactLevel = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactLevel != null) {
            src.sendSuccess(TranslationUtil.command(CMCommands.CMD_DIM_REGISTERED).withStyle(ChatFormatting.DARK_GREEN), false);
        } else {
            src.sendSuccess(TranslationUtil.command(CMCommands.CMD_DIM_NOT_FOUND).withStyle(ChatFormatting.RED), false);
        }

        try {
            final var machineData = CompactMachineData.get(serv);

            long numRegistered = machineData.stream().count();
            src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_REG_COUNT, numRegistered), false);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_DIM_NOT_FOUND));
        }

        try {
            final var roomData = CompactRoomData.get(serv);

            long numRegistered = roomData.stream().count();
            src.sendSuccess(TranslationUtil.command(CMCommands.ROOM_REG_COUNT, numRegistered), false);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_DIM_NOT_FOUND));
        }

        return 0;
    }
}
