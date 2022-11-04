package dev.compactmods.machines.room;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.room.ui.overlay.RoomMetadataDebugOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoomClientEventHandler {

    @SubscribeEvent
    public static void onOverlayRegistration(final RegisterGuiOverlaysEvent overlays) {
        overlays.registerAbove(VanillaGuiOverlay.DEBUG_TEXT.id(), "room_meta_debug", new RoomMetadataDebugOverlay());
    }
}
