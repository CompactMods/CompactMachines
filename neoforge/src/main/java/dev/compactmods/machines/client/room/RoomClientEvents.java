package dev.compactmods.machines.client.room;

import dev.compactmods.machines.client.keybinds.room.RoomExitKeyMapping;
import dev.compactmods.machines.machine.client.MachineUI;
import dev.robotgryphon.screenlib.ui.api.UiScreens;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.client.RoomKeyMappings;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class RoomClientEvents {

    public static void registerMenuScreens(final RegisterMenuScreensEvent evt) {
        UiScreens.register(evt, Machines.MACHINE_UI_MENU.get(), MachineUI::create);
    }

    public static void onKeybindRegistration(final RegisterKeyMappingsEvent evt) {
        evt.registerCategory(RoomKeyMappings.CATEGORY);
        evt.register(RoomExitKeyMapping.MAPPING);
    }

    public static void handleKeybinds(final ClientTickEvent.Post clientTick) {
        if (RoomExitKeyMapping.MAPPING.consumeClick())
            RoomExitKeyMapping.handle();
    }

    public static void onOverlayRegistration(final RegisterGuiLayersEvent layers) {
        // FIXME: Register a debug thing correctly
//        layers.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, CompactMachinesCore.identifier("room_meta_debug"), new RoomMetadataDebugOverlay());
    }
}
