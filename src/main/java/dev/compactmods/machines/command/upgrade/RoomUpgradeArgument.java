package dev.compactmods.machines.command.upgrade;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.command.CMCommandRoot;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.upgrade.MachineRoomUpgrades;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
