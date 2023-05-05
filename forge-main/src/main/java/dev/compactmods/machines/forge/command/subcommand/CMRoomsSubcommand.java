package dev.compactmods.machines.forge.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineTags;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

import java.util.stream.Collectors;

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

        if (!level.getBlockState(block).is(MachineTags.BLOCK))
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));

        if (level.getBlockEntity(block) instanceof BoundCompactMachineBlockEntity be) {
            be.connectedRoom().ifPresent(roomCode -> {
                CompactRoomProvider.instance(ctx.getSource().getServer())
                        .forRoom(roomCode)
                        .ifPresent(roomInfo -> {
                            final var m = TranslationUtil.message(Messages.MACHINE_ROOM_INFO, block, roomInfo.dimensions(), roomCode);
                            ctx.getSource().sendSuccess(m, false);
                        });
            });
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var player = EntityArgument.getPlayer(ctx, "player");

        final var playerChunk = player.chunkPosition();
        final var playerLevel = player.getLevel();

        if (!playerLevel.dimension().equals(CompactDimension.LEVEL_KEY)) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.WRONG_DIMENSION));
        }

        final var m = TranslationUtil.message(Messages.PLAYER_ROOM_INFO, player.getDisplayName(), playerChunk.toString());
        ctx.getSource().sendSuccess(m, false);

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var source = ctx.getSource();
        final var graph = CompactRoomProvider.instance(source.getServer());

        // TODO Localization
        final var owned = graph.findByOwner(owner.getUUID()).collect(Collectors.toSet());
        if (owned.isEmpty()) {
            source.sendSuccess(Component.literal("No rooms found."), false);
        } else {
            owned.forEach(roomInfo -> source.sendSuccess(Component.literal("Room: " + roomInfo.code()), false));
        }


        return 0;
    }
}
