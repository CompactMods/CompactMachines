package dev.compactmods.machines.upgrades.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import dev.compactmods.machines.upgrades.system.RoomSystems;
import dev.compactmods.machines.upgrades.system.UpgradeLifecycle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/// `/compactmachines upgrades enable|disable|list <room> [<compiled_upgrade>]`
///
/// Toggles which [CompiledRoomUpgrade] bundles are enabled on a room (stored in `RoomSystems.ENABLED_UPGRADES`).
public class RoomUpgradesSubcommand {

    public static LiteralArgumentBuilder<CommandSourceStack> make() {
        final var subRoot = Commands.literal("upgrades")
                .requires(cs -> CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(cs.enabledFeatures()))
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));

        subRoot.then(Commands.literal("enable")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(RoomUpgradesSubcommand::suggestRooms)
                        .then(Commands.argument("upgrade", IdentifierArgument.id())
                                .suggests(RoomUpgradesSubcommand::suggestCompiledUpgrades)
                                .executes(ctx -> toggle(ctx, true)))));

        subRoot.then(Commands.literal("disable")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(RoomUpgradesSubcommand::suggestRooms)
                        .then(Commands.argument("upgrade", IdentifierArgument.id())
                                .suggests(RoomUpgradesSubcommand::suggestCompiledUpgrades)
                                .executes(ctx -> toggle(ctx, false)))));

        subRoot.then(Commands.literal("list")
                .then(Commands.argument("room", StringArgumentType.string())
                        .suggests(RoomUpgradesSubcommand::suggestRooms)
                        .executes(RoomUpgradesSubcommand::list)));

        return subRoot;
    }

    private static Optional<RoomInstance> room(CommandContext<CommandSourceStack> ctx) {
        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(ctx.getSource().getServer());
        if (rooms == null) return Optional.empty();
        return rooms.get(StringArgumentType.getString(ctx, "room"));
    }

    private static int toggle(CommandContext<CommandSourceStack> ctx, boolean enable) {
        final var src = ctx.getSource();
        final var roomOpt = room(ctx);
        if (roomOpt.isEmpty()) {
            src.sendFailure(Component.literal("No such room: " + StringArgumentType.getString(ctx, "room")));
            return 0;
        }

        final Identifier id = IdentifierArgument.getId(ctx, "upgrade");
        final ResourceKey<CompiledRoomUpgrade> key = ResourceKey.create(CompiledRoomUpgrade.REGISTRY_KEY, id);
        final var registry = src.getServer().registryAccess().lookupOrThrow(CompiledRoomUpgrade.REGISTRY_KEY);
        final CompiledRoomUpgrade upgrade = registry.getValue(key);
        if (upgrade == null) {
            src.sendFailure(Component.literal("Unknown compiled room upgrade: " + id));
            return 0;
        }

        final RoomInstance room = roomOpt.get();
        final var enabled = new HashSet<>(room.getData(RoomSystems.ENABLED_UPGRADES));
        final boolean changed = enable ? enabled.add(key) : enabled.remove(key);
        room.setData(RoomSystems.ENABLED_UPGRADES, enabled);

        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(src.getServer());
        if (rooms != null) rooms.save();

        if (!changed) {
            src.sendSuccess(() -> Component.literal((enable ? "Already enabled: " : "Not enabled: ") + id), false);
            return 0;
        }

        // Fire the non-ticking lifecycle hooks for the bundle's components (e.g. chunk-loader force-load).
        if (enable) UpgradeLifecycle.onEnabled(room, upgrade);
        else UpgradeLifecycle.onDisabled(room, upgrade);

        src.sendSuccess(() -> Component.literal((enable ? "Enabled " : "Disabled ") + id + " on room " + room.code()), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        final var src = ctx.getSource();
        final var roomOpt = room(ctx);
        if (roomOpt.isEmpty()) {
            src.sendFailure(Component.literal("No such room: " + StringArgumentType.getString(ctx, "room")));
            return 0;
        }

        final var enabled = roomOpt.get().getData(RoomSystems.ENABLED_UPGRADES);
        if (enabled.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No room upgrades enabled."), false);
            return 0;
        }

        final String joined = enabled.stream()
                .map(k -> k.identifier().toString())
                .sorted()
                .collect(Collectors.joining(", "));
        src.sendSuccess(() -> Component.literal("Enabled room upgrades (" + enabled.size() + "): " + joined), false);
        return enabled.size();
    }

    private static CompletableFuture<Suggestions> suggestRooms(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(ctx.getSource().getServer());
        if (rooms == null) return builder.buildFuture();
        return SharedSuggestionProvider.suggest(rooms.allRoomCodes(), builder);
    }

    private static CompletableFuture<Suggestions> suggestCompiledUpgrades(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        final var registry = ctx.getSource().getServer().registryAccess().lookupOrThrow(CompiledRoomUpgrade.REGISTRY_KEY);
        return SharedSuggestionProvider.suggestResource(registry.keySet(), builder);
    }
}
