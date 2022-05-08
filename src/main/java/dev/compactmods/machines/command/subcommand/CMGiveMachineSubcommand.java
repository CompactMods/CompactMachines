package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.exceptions.InvalidMachineStateException;
import dev.compactmods.machines.machine.exceptions.NonexistentMachineException;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;

import javax.crypto.Mac;

public class CMGiveMachineSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("give")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

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
            CompactMachines.LOGGER.error("Error giving player a new machine block: compact dimension not found.");
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.LEVEL_NOT_FOUND));
        }

        try {
            ItemStack newItem = Machines.createBoundItem(server, roomPos);
            if(!player.addItem(newItem)) {
                src.sendFailure(TranslationUtil.command(CMCommands.CANNOT_GIVE_MACHINE));
            } else {
                src.sendSuccess(TranslationUtil.command(CMCommands.MACHINE_GIVEN, player.getDisplayName()), true);
            }

        } catch (MissingDimensionException | NonexistentRoomException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
