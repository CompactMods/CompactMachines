package dev.compactmods.machines.client.room;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.keybinds.room.RoomExitKeyMapping;
import dev.compactmods.machines.client.keybinds.room.RoomUpgradeUIMapping;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.ui.overlay.RoomMetadataDebugOverlay;
import dev.compactmods.machines.room.ui.upgrades.RoomUpgradeScreen;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public class RoomClientEvents {

    public static void registerMenuScreens(final RegisterMenuScreensEvent evt) {
        evt.register(Rooms.Menus.ROOM_UPGRADES.get(), RoomUpgradeScreen::new);
    }

    public static void onKeybindRegistration(final RegisterKeyMappingsEvent evt) {
        evt.register(RoomExitKeyMapping.MAPPING);
        evt.register(RoomUpgradeUIMapping.MAPPING);
    }

    public static void handleKeybinds(final ClientTickEvent.Post ignoredClientTick) {
        if (RoomExitKeyMapping.MAPPING.consumeClick())
            RoomExitKeyMapping.handle();

        if(RoomUpgradeUIMapping.MAPPING.consumeClick())
            RoomUpgradeUIMapping.handle();
    }

    public static void onOverlayRegistration(final RegisterGuiLayersEvent layers) {
        layers.registerAbove(VanillaGuiLayers.DEBUG_OVERLAY, CompactMachines.modRL("room_meta_debug"), new RoomMetadataDebugOverlay());
    }
}
