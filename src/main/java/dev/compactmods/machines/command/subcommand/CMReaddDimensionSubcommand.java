package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.util.DimensionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CMReaddDimensionSubcommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        return Commands.literal("registerdim")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(CMReaddDimensionSubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var src = ctx.getSource();
        var serv = src.getServer();

        var compactLevel = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactLevel == null) {
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND).withStyle(ChatFormatting.RED), false);

            DimensionUtil.createAndRegisterWorldAndDimension(serv);
        } else {
            src.sendSuccess(TranslationUtil.command(CMCommands.LEVEL_REGISTERED).withStyle(ChatFormatting.DARK_GREEN), false);
        }

        return 0;
    }


}
