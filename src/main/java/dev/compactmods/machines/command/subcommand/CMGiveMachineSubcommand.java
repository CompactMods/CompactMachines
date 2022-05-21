package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineItem;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.item.ItemStack;

public class CMGiveMachineSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("give")
                .requires(cs -> cs.hasPermission(ServerConfig.giveMachineLevel()));

        subRoot.then(Commands.argument("player", EntityArgument.player())
                .then(Commands.argument("room", RoomPositionArgument.room())
                .executes(CMGiveMachineSubcommand::giveMachine)));

        return subRoot;
    }

    private static int giveMachine(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var server = src.getServer();

        final var player = EntityArgument.getPlayer(ctx, "player");
        final var roomPos = RoomPositionArgument.get(ctx, "room");

        if(!Rooms.exists(server, roomPos)) {
            CompactMachines.LOGGER.error("Error giving player a new machine block: room not found.");
            src.sendFailure(TranslationUtil.message(Messages.UNKNOWN_ROOM_CHUNK, "%s, %s".formatted(roomPos.x, roomPos.z)));
            return -1;
        }

        try {
            final RoomSize size = Rooms.sizeOf(server, roomPos);

            ItemStack newItem = new ItemStack(CompactMachineItem.getItemBySize(size));
            CompactMachineItem.setRoom(newItem, roomPos);

            if(!player.addItem(newItem)) {
                src.sendFailure(TranslationUtil.command(CMCommands.CANNOT_GIVE_MACHINE));
            } else {
                src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_GIVEN, player.getDisplayName()), true);
            }
        } catch (NonexistentRoomException e) {
            CompactMachines.LOGGER.fatal(e);
        }

        return 0;
    }
}

