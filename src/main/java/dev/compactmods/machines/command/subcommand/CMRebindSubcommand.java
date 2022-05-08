package dev.compactmods.machines.command.subcommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.command.argument.RoomCoordinates;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.exceptions.InvalidMachineStateException;
import dev.compactmods.machines.machine.exceptions.NonexistentMachineException;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

public class CMRebindSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("rebind")
                .requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

        subRoot.then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("bindTo", RoomPositionArgument.room())
                .executes(CMRebindSubcommand::doRebind)));

        return subRoot;
    }

    private static int doRebind(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var server = ctx.getSource().getServer();
        final var level = ctx.getSource().getLevel();

        final var rebindingMachine = BlockPosArgument.getLoadedBlockPos(ctx, "pos");
        final var roomPos = RoomPositionArgument.get(ctx, "bindTo");

        CompactMachines.LOGGER.debug("Binding machine at {} to room chunk {}", rebindingMachine, roomPos);

        if(!(level.getBlockEntity(rebindingMachine) instanceof CompactMachineBlockEntity machineData)) {
            CompactMachines.LOGGER.error("Refusing to rebind block at {}; block has invalid machine data.", rebindingMachine);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.NOT_A_MACHINE_BLOCK));
        }

        if(!machineData.mapped()) {
            var linked = Machines.createAndLink(server, level, rebindingMachine, machineData, roomPos);
            if(!linked) {
                CompactMachines.LOGGER.error("Failed to register and bind new machine.");
                throw new CommandRuntimeException(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "failed_to_bind_new")));
            }
            return 1;
        }

        try {
            Machines.changeLink(server, machineData.machineId, roomPos);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.error("Failed to rebind a machine to a different room: room data not found", e);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.ROOM_DATA_NOT_FOUND));
        } catch (NonexistentRoomException e) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_ROOM_NOT_REGISTERED));
        } catch (NonexistentMachineException e) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_MACHINE_NOT_REGISTERED));
        } catch (InvalidMachineStateException e) {
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_BAD_STATE));
        }

        return 0;
    }
}
