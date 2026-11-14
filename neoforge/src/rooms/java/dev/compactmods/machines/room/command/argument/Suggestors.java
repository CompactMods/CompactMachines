package dev.compactmods.machines.room.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.Set;

public class Suggestors {
    public static final SuggestionProvider<CommandSourceStack> ROOM_TEMPLATES = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(getRegistryValues(ctx, RoomTemplate.REGISTRY_KEY), builder);

//    public static final SuggestionProvider<CommandSourceStack> OWNED_ROOM_CODES = (ctx, builder) -> {
//        final var owner = ctx.getSource().getPlayerOrException();
//
//        final var codes = CompactMachinesCore.roomApi().owners()
//                .findByOwner(owner.getUUID())
//                .toList();
//
//        return SharedSuggestionProvider.suggest(codes, builder);
//    };

    public static final SuggestionProvider<CommandSourceStack> ROOM_CODES = (ctx, builder) -> {
        final var server = ctx.getSource().getServer();
        final var registry = server.getCapability(RoomCapabilities.REGISTRY);

        final var codes = registry.allRoomCodes().toList();

        return SharedSuggestionProvider.suggest(codes, builder);
    };



    private static <T> Set<Identifier> getRegistryValues(CommandContext<CommandSourceStack> ctx, ResourceKey<Registry<T>> keyType) {
        return ctx.getSource().registryAccess()
                .lookupOrThrow(keyType)
                .keySet();
    }
}
