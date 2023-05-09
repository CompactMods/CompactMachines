package dev.compactmods.machines.forge.command.argument;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class Suggestors {

    public static final SuggestionProvider<CommandSourceStack> ROOM_UPGRADES = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(MachineRoomUpgrades.REGISTRY.get().getKeys(), builder);

    public static final SuggestionProvider<CommandSourceStack> OWNED_ROOM_CODES = (ctx, builder) -> {
        final var server = ctx.getSource().getServer();
        final var owner = ctx.getSource().getPlayerOrException();
        final var roomProvider = CompactRoomProvider.instance(server);
        final var codes = roomProvider.findByOwner(owner.getUUID()).map(IRoomRegistration::code);
        return SharedSuggestionProvider.suggest(codes, builder);
    };

    public static final SuggestionProvider<CommandSourceStack> ROOM_CODES = (ctx, builder) -> {
        final var server = ctx.getSource().getServer();
        final var roomProvider = CompactRoomProvider.instance(server);
        final var codes = roomProvider.allRooms().map(IRoomRegistration::code);
        return SharedSuggestionProvider.suggest(codes, builder);
    };
}
