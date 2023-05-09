package dev.compactmods.machines.forge.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.core.CMRegistryKeys;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class RoomUpgradeArgument extends ResourceKeyArgument<RoomUpgrade> {

    private static final DynamicCommandExceptionType ERROR_INVALID_UPGRADE = new DynamicCommandExceptionType((a) ->
            TranslationUtil.command(CMCommands.WRONG_DIMENSION));

    private RoomUpgradeArgument() {
        super(CMRegistryKeys.ROOM_UPGRADES);
    }

    public static Optional<RoomUpgrade> getUpgrade(CommandContext<CommandSourceStack> stack, String argName) throws CommandSyntaxException {
        final var UPGRADES = MachineRoomUpgrades.REGISTRY.get();
        ResourceKey<RoomUpgrade> resourcekey = getRegistryType(stack, argName, CMRegistryKeys.ROOM_UPGRADES, ERROR_INVALID_UPGRADE);
        return Optional.ofNullable(UPGRADES.getValue(resourcekey.location()));
    }

    public static RoomUpgradeArgument upgrade() {
        return new RoomUpgradeArgument();
    }
}
