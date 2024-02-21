package dev.compactmods.machines.neoforge.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.command.CMCommands;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.api.util.BlockSpaceUtil;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

public class CMRoomsSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final LiteralArgumentBuilder<CommandSourceStack> subRoot = LiteralArgumentBuilder.literal("rooms");

        subRoot.then(Commands.literal("machblock").then(
                Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(CMRoomsSubcommand::fetchByMachineBlock)
        ));

        subRoot.then(Commands.literal("findplayer").then(
                Commands.argument("player", EntityArgument.player())
                        .executes(CMRoomsSubcommand::findByContainingPlayer)
        ));

        subRoot.then(Commands.literal("ownedby").then(
                Commands.argument("owner", EntityArgument.player())
                        .executes(CMRoomsSubcommand::findByOwner)
        ));

        return subRoot;
    }

    private static int fetchByMachineBlock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var block = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var level = ctx.getSource().getLevel();

        if (!level.getBlockState(block).is(MachineConstants.MACHINE_BLOCK)) {
            ctx.getSource().sendFailure(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
            return -1;
        }

        if (level.getBlockEntity(block) instanceof BoundCompactMachineBlockEntity be) {
            final var roomCode = be.connectedRoom();
            RoomApi.registrar().get(roomCode).ifPresent(roomInfo -> {
                final var innerBounds = roomInfo.boundaries().innerBounds();
                final var m = TranslationUtil.message(Messages.MACHINE_ROOM_INFO, block, BlockSpaceUtil.aabbToString(innerBounds), roomCode);
                ctx.getSource().sendSuccess(() -> m, false);
            });
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var source = ctx.getSource();

        final var player = EntityArgument.getPlayer(ctx, "player");

        final var playerChunk = player.chunkPosition();
        final var playerLevel = player.level();

        if (!playerLevel.dimension().equals(CompactDimension.LEVEL_KEY)) {
            source.sendFailure(TranslationUtil.command(CMCommands.WRONG_DIMENSION));
            return -1;
        }

        final var m = TranslationUtil.message(Messages.PLAYER_ROOM_INFO, player.getDisplayName(), playerChunk.toString());
        source.sendSuccess(() -> m, false);

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var source = ctx.getSource();

        final var owned = RoomApi.owners().findByOwner(owner.getUUID()).toList();

        // TODO Localization
        if (owned.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No rooms found."), false);
        } else {
            owned.forEach(roomCode -> source.sendSuccess(() -> Component.literal("Room: " + roomCode), false));
        }


        return 0;
    }
}
