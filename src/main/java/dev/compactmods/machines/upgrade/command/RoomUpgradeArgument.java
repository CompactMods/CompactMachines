package dev.compactmods.machines.upgrade.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.upgrade.MachineRoomUpgrades;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

public class RoomUpgradeArgument extends ResourceKeyArgument<RoomUpgrade> {

    public static final SuggestionProvider<CommandSourceStack> SUGGESTOR = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(MachineRoomUpgrades.REGISTRY.get().getKeys(), builder);

    private static final DynamicCommandExceptionType ERROR_INVALID_UPGRADE = new DynamicCommandExceptionType((a) ->
            TranslationUtil.command(CMCommands.WRONG_DIMENSION));

    private RoomUpgradeArgument() {
        super(MachineRoomUpgrades.REGISTRY.get().getRegistryKey());
    }

    public static Optional<RoomUpgrade> getUpgrade(CommandContext<CommandSourceStack> stack, String argName) throws CommandSyntaxException {
        final var UPGRADES = MachineRoomUpgrades.REGISTRY.get();
        ResourceKey<RoomUpgrade> resourcekey = getRegistryType(stack, argName, MachineRoomUpgrades.REGISTRY_KEY, ERROR_INVALID_UPGRADE);
        return Optional.ofNullable(UPGRADES.getValue(resourcekey.location()));
    }

    public static RoomUpgradeArgument upgrade() {
        return new RoomUpgradeArgument();
    }
}
