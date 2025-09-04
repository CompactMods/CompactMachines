package dev.compactmods.machines.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.ReloadCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.List;

public class EnableBasicTemplatesSubcommand {

    final static String PACK_ID = "mod/compactmachines:data/compactmachines/datapacks/basic_templates";

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        return Commands.literal("enable_basic_templates")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS) &&
                        RoomTemplateHelper.getTemplates(cs.registryAccess()).findAny().isEmpty())
                .executes(EnableBasicTemplatesSubcommand::exec);
    }

    private static int exec(CommandContext<CommandSourceStack> ctx) {
        PackRepository packrepository = ctx.getSource().getServer().getPackRepository();

        List<Pack> list = Lists.newArrayList(packrepository.getSelectedPacks());
        if(!packrepository.getSelectedIds().contains(PACK_ID)) {
            final var pack = packrepository.getPack(PACK_ID);
            if (pack != null) {
                pack.getDefaultPosition().insert(list, pack, Pack::selectionConfig, false);
                ReloadCommand.reloadPacks(list.stream().map(Pack::getId).toList(), ctx.getSource());

                ctx.getSource().sendSuccess(() -> Component.literal("Enabled basic templates, restart game to sync changes."), false);
            }
        } else {
            ctx.getSource().sendFailure(Component.literal("Already enabled, restart game to sync changes."));
        }

        return 0;
    }
}
