package dev.compactmods.machines.upgrades.command;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public interface RUCommands {

    SuggestionProvider<CommandSourceStack> ROOM_UPGRADE_TYPES = (context, builder)
            -> SharedSuggestionProvider.listSuggestions(context, builder, RoomUpgradeComponentType.REGISTRY_KEY, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);

}
