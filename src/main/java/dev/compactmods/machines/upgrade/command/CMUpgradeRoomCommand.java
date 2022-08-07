package dev.compactmods.machines.upgrade.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrade.RoomUpgradeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CMUpgradeRoomCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var root = Commands.literal("upgrades")
                .requires(cs -> cs.hasPermission(ServerConfig.changeUpgrades()));

        final var addRoot = Commands.literal("add");
        final var addUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR)
                .executes(CMUpgradeRoomCommand::addToCurrentRoom);

        addUpgRoot.then(Commands.literal("current").executes(CMUpgradeRoomCommand::addToCurrentRoom));
        // addUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::addToSpecificRoom));
        addRoot.then(addUpgRoot);
        root.then(addRoot);

        final var remRoot = Commands.literal("remove");
        final var remUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR)
                .executes(CMUpgradeRoomCommand::removeFromCurrentRoom);

        remUpgRoot.then(Commands.literal("current").executes(CMUpgradeRoomCommand::removeFromCurrentRoom));
        // remUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::removeFromSpecificRoom));
        remRoot.then(remUpgRoot);
        root.then(remRoot);

        return root;
    }

    private static int addToCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var level = ctx.getSource().getLevel();
        final var serv = ctx.getSource().getServer();

        if (!level.dimension().equals(CompactDimension.LEVEL_KEY))
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
        if(manager.hasUpgrade(currChunk, upgrade)) {
            src.sendFailure(TranslationUtil.message(Messages.ALREADY_HAS_UPGRADE));
        } else {
            final var added = manager.addUpgrade(upgrade, currChunk);

            if (added) {
                upgrade.onAdded(level, currChunk);
                src.sendSuccess(TranslationUtil.message(Messages.UPGRADE_APPLIED), true);
            } else {
                src.sendFailure(TranslationUtil.message(Messages.UPGRADE_ADD_FAILED));
            }
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

        if (!level.dimension().equals(CompactDimension.LEVEL_KEY))
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
        if(!manager.hasUpgrade(currChunk, upgrade)) {
            src.sendFailure(TranslationUtil.message(Messages.UPGRADE_NOT_PRESENT));
        } else {
            final var removed = manager.removeUpgrade(upgrade, currChunk);

            if (removed) {
                upgrade.onRemoved(level, currChunk);
                src.sendSuccess(TranslationUtil.message(Messages.UPGRADE_REMOVED), true);
            } else {
                src.sendFailure(TranslationUtil.message(Messages.UPGRADE_REM_FAILED));
            }
        }

        return 0;
    }

    private static int removeFromSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }


}
