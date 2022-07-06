package dev.compactmods.machines.upgrade.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrade.RoomUpgradeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;

public class CMUpgradeRoomCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var root = Commands.literal("upgrades");

        final var addRoot = Commands.literal("add");
        final var addUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR);

        addUpgRoot.then(Commands.literal("current").executes(CMUpgradeRoomCommand::addToCurrentRoom));
        // addUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::addToSpecificRoom));
        addRoot.then(addUpgRoot);

        final var remRoot = Commands.literal("remove");
        final var remUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR);
        remUpgRoot.then(Commands.literal("current").executes(CMUpgradeRoomCommand::removeFromCurrentRoom));
        // remUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::removeFromSpecificRoom));
        remRoot.then(remRoot);

        return root;
    }

    private static int addToCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var level = ctx.getSource().getLevel();
        final var serv = ctx.getSource().getServer();

        if (!level.dimension().equals(Registration.COMPACT_DIMENSION))
            return -1;

        final var upg = RoomUpgradeArgument.getUpgrade(ctx, "upgrade");

        if (upg.isEmpty())
            return -1;

        final var upgrade = upg.get();

        final var execdAt = src.getPosition();
        final var currChunk = new ChunkPos(new BlockPos((int) execdAt.x, (int) execdAt.y, (int) execdAt.z));

        if (!Rooms.exists(serv, currChunk))
            return -1;

        final var manager = RoomUpgradeManager.get(level);
        final var added = manager.addUpgrade(upgrade, currChunk);

        if (added) {
            src.sendSuccess(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_applied")), true);
        } else {
            src.sendFailure(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_not_applied")));
        }

        return 0;
    }

    private static int addToSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }

    private static int removeFromCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var level = ctx.getSource().getLevel();
        final var serv = ctx.getSource().getServer();

        if (!level.dimension().equals(Registration.COMPACT_DIMENSION))
            return -1;

        final var upg = RoomUpgradeArgument.getUpgrade(ctx, "upgrade");

        if (upg.isEmpty())
            return -1;

        final var upgrade = upg.get();

        final var execdAt = src.getPosition();
        final var currChunk = new ChunkPos(new BlockPos((int) execdAt.x, (int) execdAt.y, (int) execdAt.z));

        if (!Rooms.exists(serv, currChunk))
            return -1;

        final var manager = RoomUpgradeManager.get(level);
        final var removed = manager.removeUpgrade(upgrade, currChunk);

        if (removed) {
            src.sendSuccess(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_removed")), true);
        } else {
            src.sendFailure(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_not_removed")));
        }

        return 0;
    }

    private static int removeFromSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }


}
