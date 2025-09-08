package dev.compactmods.machines.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponentType;
import dev.compactmods.machines.api.room.upgrade.component.RoomUpgradeComponentList;
import dev.compactmods.machines.command.argument.Suggestors;
import dev.compactmods.machines.feature.CMFeatureFlags;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomUpgradesSubcommand {

    private static final Logger LOGGER = LoggingUtil.modLog();

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("upgrades")
                .requires(cs -> CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(cs.enabledFeatures())
                        && cs.hasPermission(Commands.LEVEL_GAMEMASTERS));

        // /cm upgrades add [id]
        subRoot.then(Commands.literal("add")
                .then(Commands.argument("upgrade", ResourceLocationArgument.id())
                        .suggests(Suggestors.ROOM_UPGRADE_TYPES)
                        .executes(RoomUpgradesSubcommand::applyUpgrade)));

        // /cm upgrades remove [id]
        subRoot.then(Commands.literal("remove")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(Suggestors.ROOM_UPGRADE_TYPES)
                        .executes(RoomUpgradesSubcommand::removeUpgrade)));

        return subRoot;
    }

    private static Optional<RoomUpgradeComponentType<?>> getTargetedUpgradeType(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();

        final var upgradeType = ResourceLocationArgument.getId(ctx, "upgrade");
        return src.getServer()
                .registryAccess()
                .lookupOrThrow(RoomUpgradeComponentType.REGISTRY_KEY)
                .getOptional(upgradeType);
    }

    private static int applyUpgrade(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var player = ctx.getSource().getPlayerOrException();
        var realUpgradeType = getTargetedUpgradeType(ctx);
        if(realUpgradeType.isEmpty()) return 0;

        var upgradeType = realUpgradeType.get();

        var heldItem = player.getMainHandItem();
        var currentUpgrades = heldItem.get(CMDataComponents.UPGRADE_LIST_COMPONENT);

        if(!upgradeType.canApplyTo(heldItem)) {
            ctx.getSource().sendFailure(Component.literal("That upgrade cannot be applied to the held item."));
            return 0;
        }

        if (currentUpgrades != null) {
            var addedList = new ArrayList<>(currentUpgrades.components());

            // TODO: Room Upgrade context (level, itemstack, etc)
            addedList.add(upgradeType.constructor().get());

            var newList = new RoomUpgradeComponentList(addedList);
            heldItem.set(CMDataComponents.UPGRADE_LIST_COMPONENT, newList);
        } else {
            // TODO: Room Upgrade context (level, itemstack, etc)
            var newList = new RoomUpgradeComponentList(List.of(upgradeType.constructor().get()));
            heldItem.set(CMDataComponents.UPGRADE_LIST_COMPONENT, newList);
        }

        return 0;
    }

    private static int removeUpgrade(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final var player = ctx.getSource().getPlayerOrException();
        var realUpgradeType = getTargetedUpgradeType(ctx);

        var heldItem = player.getMainHandItem();
        var currentUpgrades = heldItem.get(CMDataComponents.UPGRADE_LIST_COMPONENT);

        if (currentUpgrades != null) {
            var newList = new RoomUpgradeComponentList(currentUpgrades.components());
            newList.components().removeIf(ru -> ru.getType().equals(realUpgradeType));

            heldItem.set(CMDataComponents.UPGRADE_LIST_COMPONENT, newList);
        }

        return 0;
    }
}

