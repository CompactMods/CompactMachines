package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlock;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.TextComponent;
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
        final var server = ctx.getSource().getServer();

        if(!(level.getBlockState(block).getBlock() instanceof CompactMachineBlock b))
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));

        if(level.getBlockEntity(block) instanceof CompactMachineBlockEntity be) {
            if(!be.mapped())
                throw new CommandRuntimeException(TranslationUtil.command(CMCommands.MACHINE_NOT_BOUND, block.toShortString()));

            try {
                final var info = Machines.getConnectedRoom(server, be.machineId)
                        .orElseThrow(() -> new CommandRuntimeException(TranslationUtil.command(CMCommands.ROOM_DATA_NOT_FOUND)));

                final var size = Rooms.sizeOf(server, info.chunk());

                final var m = TranslationUtil.message(Messages.MACHINE_ROOM_INFO, block, size, info.chunk());
                ctx.getSource().sendSuccess(m, false);
            } catch (MissingDimensionException e) {
                e.printStackTrace();
            } catch (NonexistentRoomException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    private static int findByContainingPlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var player = EntityArgument.getPlayer(ctx, "player");
        final var server = ctx.getSource().getServer();

        final var playerChunk = player.chunkPosition();
        final var playerLevel = player.getLevel();

        if (!playerLevel.dimension().equals(Registration.COMPACT_DIMENSION)) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.WRONG_DIMENSION));
        }

        try {
            final var roomSize = Rooms.sizeOf(server, playerChunk);
            final var m = TranslationUtil.message(Messages.PLAYER_ROOM_INFO, player.getDisplayName(), playerChunk.toString(), roomSize);
            ctx.getSource().sendSuccess(m, false);
        } catch (MissingDimensionException e) {
            throw new CommandRuntimeException(TranslationUtil.message(Messages.UNREGISTERED_CM_DIM));
        } catch (NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Player is inside an unregistered chunk ({}) in the compact world.", playerChunk, e);
            throw new CommandRuntimeException(TranslationUtil.message(Messages.UNKNOWN_ROOM_CHUNK));
        }

        return 0;
    }

    public static int findByOwner(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var owner = EntityArgument.getPlayer(ctx, "owner");
        final var server = ctx.getSource().getServer();

        try {
            final var rooms = CompactRoomData.get(server);
            rooms.streamRooms()
                    .filter(r -> r.getOwner().equals(owner.getUUID()))
                    .forEach(data -> {
                        ctx.getSource().sendSuccess(new TextComponent("Room: " + new ChunkPos(data.getCenter())), false);
                    });
        }

        catch(MissingDimensionException e) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }
        return 0;
    }
}
