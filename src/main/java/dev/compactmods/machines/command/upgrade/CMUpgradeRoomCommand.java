package dev.compactmods.machines.command.upgrade;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.command.CMCommandRoot;
import dev.compactmods.machines.command.argument.RoomPositionArgument;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.upgrade.ChunkloadUpgrade;
import dev.compactmods.machines.room.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.room.upgrade.RoomUpgradeManager;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;

public class CMUpgradeRoomCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> make() {
        final var root = Commands.literal("upgrades");

        final var upgRoot = Commands.argument("upgrade", RoomUpgradeArgument.upgrade())
                .suggests(RoomUpgradeArgument.SUGGESTOR);

        upgRoot.then(
                Commands.literal("add")
                        .then(Commands.literal("current").executes(CMUpgradeRoomCommand::addToCurrentRoom))
                        .then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::addToSpecificRoom))
        );

        upgRoot.then(Commands.literal("remove")
                        .then(Commands.literal("current").executes(CMUpgradeRoomCommand::removeFromCurrentRoom))
                        .then(Commands.argument("room", RoomPositionArgument.room()).executes(CMUpgradeRoomCommand::removeFromSpecificRoom))
        );

        root.then(upgRoot);
        return root;
    }

    private static int addToCurrentRoom(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var src = ctx.getSource();
        final var level = ctx.getSource().getLevel();
        final var serv = ctx.getSource().getServer();

        if(!level.dimension().equals(Registration.COMPACT_DIMENSION))
            return -1;

        final var upg = RoomUpgradeArgument.getUpgrade(ctx, "upgrade");

        if(upg.isEmpty())
            return -1;

        final var upgrade = upg.get();

        final var execdAt = src.getPosition();
        final var currChunk = new ChunkPos(new BlockPos((int) execdAt.x, (int) execdAt.y, (int) execdAt.z));

        if(!Rooms.exists(serv, currChunk))
            return -1;

        final var manager = RoomUpgradeManager.get(level);
        final var added = manager.addUpgrade(upgrade, currChunk);

        if(added) {
            src.sendSuccess(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_applied")), true);
        } else {
            src.sendFailure(TranslationUtil.command(new ResourceLocation(CompactMachines.MOD_ID, "upgrade_not_applied")));
        }

        return 0;
    }

    private static int addToSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }

    private static int removeFromCurrentRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }

    private static int removeFromSpecificRoom(CommandContext<CommandSourceStack> ctx) {
        return 0;
    }


}
