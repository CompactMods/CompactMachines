package dev.compactmods.machines.command.rooms;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.i18n.RoomTranslations;
import dev.compactmods.machines.machine.block.BoundCompactMachineBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class CMFindRoomSubcommand {
    static @NotNull LiteralArgumentBuilder<CommandSourceStack> create() {
        // /cm rooms find ...
        final var find = Commands.literal("find");

        // /cm rooms find chunk
        find.then(Commands.literal("chunk").then(
                // /cm rooms find chunk [pos]
                Commands.argument("chunk", ColumnPosArgument.columnPos())
                        .executes(CMFindRoomSubcommand::fetchByChunkPos)
        ));

        // /cm rooms find connected_to
        find.then(Commands.literal("connected_to").then(
                // /cm rooms find connected_to [pos]
                Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(CMFindRoomSubcommand::fetchByMachineBlock)
        ));

        // /cm rooms find player
        find.then(Commands.literal("player").then(
                // /cm rooms find player [@p]
                Commands.argument("player", EntityArgument.player())
                        .executes(CMFindRoomSubcommand::findByContainingPlayer)
        ));

//        find.then(Commands.literal("owner").then(
//                Commands.argument("owner", EntityArgument.player())
//                        .executes(CMRoomsSubcommand::findByOwner)
//        ));
        return find;
    }

    private static int fetchByChunkPos(CommandContext<CommandSourceStack> ctx) {
        final var chunkPos = ColumnPosArgument.getColumnPos(ctx, "chunk");

        final var m = CompactMachines.chunkManager()
                .findRoomByChunk(chunkPos.toChunkPos())

                // FIXME Translations
                .map(code -> Component.translatableWithFallback("commands.cm.room_by_chunk", "Room at chunk %s has ID: %s", chunkPos.toString(), code))
                .orElse(Component.literal("Room not found at chunk: " + chunkPos));

        ctx.getSource().sendSuccess(() -> m, false);

        return 0;
    }

    private static int fetchByMachineBlock(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var block = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var level = ctx.getSource().getLevel();

        if (!level.getBlockState(block).is(MachineConstants.MACHINE_BLOCK)) {
            ctx.getSource().sendFailure(MachineTranslations.NOT_A_MACHINE_BLOCK.apply(block));
            return -1;
        }

        if (level.getBlockEntity(block) instanceof BoundCompactMachineBlockEntity be) {
            final var roomCode = be.connectedRoom();
            CompactMachines.room(roomCode).ifPresent(roomInfo -> {
                ctx.getSource().sendSuccess(() -> RoomTranslations.MACHINE_ROOM_INFO.apply(block, roomInfo), false);
            });
        } else {
            // FIXME Translations
            ctx.getSource().sendFailure(Component.literal("Does not appear to be a bound machine block."));
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var source = ctx.getSource();

        final var player = EntityArgument.getPlayer(ctx, "player");

        if (!player.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
            source.sendFailure(RoomTranslations.PLAYER_NOT_IN_COMPACT_DIM.apply(player));
            return -1;
        }

        final var m = CompactMachines.chunkManager()
                .findRoomByChunk(player.chunkPosition())
                .map(code -> RoomTranslations.PLAYER_ROOM_INFO.apply(player, code))
                .orElse(RoomTranslations.UNKNOWN_ROOM_BY_PLAYER_CHUNK.apply(player));

        source.sendSuccess(() -> m, false);

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var source = ctx.getSource();

//        final var owned = CompactMachines.roomApi().owners().findByOwner(owner.getUUID()).toList();
//
//        // TODO Localization
//        if (owned.isEmpty()) {
//            source.sendSuccess(() -> Component.literal("No rooms found."), false);
//        } else {
//            owned.forEach(roomCode -> source.sendSuccess(() -> Component.literal("Room: " + roomCode), false));
//        }


        return 0;
    }
}
