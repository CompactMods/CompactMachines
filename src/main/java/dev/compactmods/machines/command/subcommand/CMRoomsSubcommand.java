package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

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

        if(!(level.getBlockState(block).getBlock() instanceof CompactMachineBlock b))
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));

        if(level.getBlockEntity(block) instanceof CompactMachineBlockEntity be) {
            be.getConnectedRoom().ifPresent(room -> {
                final var m = TranslationUtil.message(Messages.MACHINE_ROOM_INFO, block, b.getSize(), room);
                ctx.getSource().sendSuccess(m, false);
            });
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var server = ctx.getSource().getServer();

        final var playerChunk = player.chunkPosition();
        final var playerLevel = player.getLevel();

        if (!playerLevel.dimension().equals(Dimension.COMPACT_DIMENSION)) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.WRONG_DIMENSION));
        }

        try {
            final var roomSize = Rooms.sizeOf(server, playerChunk);
            final var m = TranslationUtil.message(Messages.PLAYER_ROOM_INFO, player.getDisplayName(), playerChunk.toString(), roomSize);
            ctx.getSource().sendSuccess(m, false);
        } catch (NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Player is inside an unregistered chunk ({}) in the compact world.", playerChunk, e);
            final var tc = Component.literal("%s, %s".formatted(playerChunk.x, playerChunk.z))
                    .withStyle(ChatFormatting.RED);

            throw new CommandRuntimeException(TranslationUtil.message(Messages.UNKNOWN_ROOM_CHUNK, tc));
        }

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var server = ctx.getSource().getServer();
        final var compactDim = server.getLevel(Dimension.COMPACT_DIMENSION);

        final var rooms = CompactRoomData.get(compactDim);
        rooms.streamRooms()
                .filter(r -> r.getOwner().equals(owner.getUUID()))
                .forEach(data -> {
                    ctx.getSource().sendSuccess(Component.literal("Room: " + new ChunkPos(data.getCenter())), false);
                });

        return 0;
    }
}
