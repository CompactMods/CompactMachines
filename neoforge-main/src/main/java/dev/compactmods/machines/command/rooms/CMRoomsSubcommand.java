package dev.compactmods.machines.command.rooms;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.i18n.CommandTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.stream.LongStream;

public class CMRoomsSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        // /cm rooms
        final LiteralArgumentBuilder<CommandSourceStack> subRoot = LiteralArgumentBuilder.literal("rooms");

        // TODO: /cm rooms create [size]

        // /cm rooms summary
        final var summary = Commands.literal("summary")
                .executes(CMRoomsSubcommand::execRoomSummary);

        subRoot.then(summary);
        subRoot.then(CMFindRoomSubcommand.create());
        return subRoot;
    }

    private static int execRoomSummary(CommandContext<CommandSourceStack> ctx) {
        var src = ctx.getSource();
        var serv = src.getServer();

        final var ls = LongStream.builder();

        // FIXME: Per-dimension machine count
//        serv.getAllLevels().forEach(sl -> {
//            final var machineData = DimensionMachineGraph.forDimension(sl);
//            long numRegistered = machineData.machines().count();
//
//            if(numRegistered > 0) {
//                src.sendSuccess(() -> TranslationUtil.command(CMCommands.MACHINE_REG_DIM, sl.dimension().location().toString(), numRegistered), false);
//                ls.add(numRegistered);
//            }
//        });

//        long grandTotal = ls.build().sum();
//        src.sendSuccess(() -> Component.translatable(CommandTranslations.IDs.MACHINE_REG_TOTAL, grandTotal).withStyle(ChatFormatting.GOLD), false);

        final var roomCount = CompactMachines.roomRegistrar().count();
        src.sendSuccess(() -> Component.translatable(CommandTranslations.IDs.ROOM_COUNT, roomCount), false);

        return 0;
    }

}
