package dev.compactmods.machines.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.util.TranslationUtil;
import net.minecraft.ChatFormatting;
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
        if(compactLevel != null) {
            src.sendSuccess(TranslationUtil.command("level_registered").withStyle(ChatFormatting.DARK_GREEN), false);
        } else {
            src.sendSuccess(TranslationUtil.command("level_not_found").withStyle(ChatFormatting.RED), false);
        }

        var machineData = CompactMachineData.get(serv);
        if(machineData != null) {
            long numRegistered = machineData.stream().count();
            src.sendSuccess(TranslationUtil.command("machine_reg_count", numRegistered), false);
        }

        var roomData = CompactRoomData.get(serv);
        if(roomData != null) {
            long numRegistered = roomData.stream().count();
            src.sendSuccess(TranslationUtil.command("room_reg_count", numRegistered), false);
        }

        return 0;
    }
}
