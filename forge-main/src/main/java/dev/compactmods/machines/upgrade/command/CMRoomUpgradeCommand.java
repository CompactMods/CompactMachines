package dev.compactmods.machines.upgrade.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.upgrade.RoomUpgradeManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CMRoomUpgradeCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var root = Commands.literal("upgrades")
                .requires(cs -> cs.hasPermission(ServerConfig.changeUpgrades()));

        final var addRoot = Commands.literal("add");
        final var addUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR)
                .executes(CMRoomUpgradeCommand::addToCurrentRoom);

        addUpgRoot.then(Commands.literal("current").executes(CMRoomUpgradeCommand::addToCurrentRoom));
        // addUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::addToSpecificRoom));
        addRoot.then(addUpgRoot);
        root.then(addRoot);

        final var remRoot = Commands.literal("remove");
        final var remUpgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR)
                .executes(CMRoomUpgradeCommand::removeFromCurrentRoom);

        remUpgRoot.then(Commands.literal("current").executes(CMRoomUpgradeCommand::removeFromCurrentRoom));
        // remUpgRoot.then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::removeFromSpecificRoom));
        remRoot.then(remUpgRoot);
        root.then(remRoot);

        return root;
    }

    private static int addToCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var compactDim = ctx.getSource().getLevel();
        final var serv = ctx.getSource().getServer();

        if (!compactDim.dimension().equals(CompactDimension.LEVEL_KEY))
            return -1;

        final var upg = RoomUpgradeArgument.getUpgrade(ctx, "upgrade");

        if (upg.isEmpty())
            return -1;

        final var upgrade = upg.get();

        final var execdAt = src.getPosition();
        final var currChunk = new ChunkPos(new BlockPos((int) execdAt.x, (int) execdAt.y, (int) execdAt.z));

        final var roomProvider = CompactRoomProvider.instance(compactDim);
        final var manager = RoomUpgradeManager.get(compactDim);

        if (!roomProvider.isRoomChunk(currChunk)) {
            src.sendFailure(TranslationUtil.command(CMCommands.NOT_IN_COMPACT_DIMENSION));
            return -1;
        }

        roomProvider.findByChunk(currChunk).ifPresentOrElse(room -> {
            if (manager.hasUpgrade(room.code(), upgrade)) {
                src.sendFailure(TranslationUtil.message(Messages.ALREADY_HAS_UPGRADE));
            } else {
                final var added = manager.addUpgrade(upgrade, room.code());

                if (added) {
                    upgrade.onAdded(compactDim, room);
                    src.sendSuccess(TranslationUtil.message(Messages.UPGRADE_APPLIED), true);
                } else {
                    src.sendFailure(TranslationUtil.message(Messages.UPGRADE_ADD_FAILED));
                }
            }
        }, () -> src.sendFailure(TranslationUtil.command(CMCommands.NOT_IN_COMPACT_DIMENSION)));


        return 0;
    }

    private static int addToSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }

    private static int removeFromCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var compactDim = ctx.getSource().getLevel();

        if (!compactDim.dimension().equals(CompactDimension.LEVEL_KEY))
            return -1;

        final var upg = RoomUpgradeArgument.getUpgrade(ctx, "upgrade");

        if (upg.isEmpty())
            return -1;

        final var upgrade = upg.get();

        final var execdAt = src.getPosition();
        final var currChunk = new ChunkPos(new BlockPos((int) execdAt.x, (int) execdAt.y, (int) execdAt.z));

        final var roomProvider = CompactRoomProvider.instance(compactDim);
        final var manager = RoomUpgradeManager.get(compactDim);

        if (!roomProvider.isRoomChunk(currChunk)) {
            src.sendFailure(TranslationUtil.command(CMCommands.NOT_IN_COMPACT_DIMENSION));
            return -1;
        }

        roomProvider.findByChunk(currChunk).ifPresentOrElse(room -> {
            if (!manager.hasUpgrade(room.code(), upgrade)) {
                src.sendFailure(TranslationUtil.message(Messages.UPGRADE_NOT_PRESENT));
            } else {
                final var removed = manager.removeUpgrade(upgrade, room.code());

                if (removed) {
                    upgrade.onRemoved(compactDim, room);
                    src.sendSuccess(TranslationUtil.message(Messages.UPGRADE_REMOVED), true);
                } else {
                    src.sendFailure(TranslationUtil.message(Messages.UPGRADE_REM_FAILED));
                }
            }
        }, () -> src.sendFailure(TranslationUtil.command(CMCommands.NOT_IN_COMPACT_DIMENSION)));

        return 0;
    }

    private static int removeFromSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }
}
