package dev.compactmods.machines.core;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public interface Translations {

    Supplier<Component> HINT_HOLD_SHIFT = () -> Component.translatableWithFallback(IDs.HINT_HOLD_SHIFT, "Hold shift for details.")
            .withStyle(ChatFormatting.DARK_GRAY)
            .withStyle(ChatFormatting.ITALIC);

    Supplier<Component> TELEPORT_OUT_OF_BOUNDS = () -> Component
            .translatableWithFallback(IDs.TELEPORT_OUT_OF_BOUNDS, "An otherworldly force prevents your teleportation.")
            .withStyle(ChatFormatting.DARK_RED)
            .withStyle(ChatFormatting.ITALIC);

    interface IDs {
        String TELEPORT_OUT_OF_BOUNDS = Util.makeDescriptionId("messages", CompactMachinesCore.identifier("teleport_oob"));
        String HOW_DID_YOU_GET_HERE = Util.makeDescriptionId("messages", CompactMachinesCore.identifier("how_did_you_get_here"));
        String HINT_HOLD_SHIFT = Util.makeDescriptionId("messages", CompactMachinesCore.identifier("hint.hold_shift"));

    }
}
