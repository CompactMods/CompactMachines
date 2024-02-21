package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RoomClientEventHandler {

    @SubscribeEvent
    public static void onOverlayRegistration(final RegisterGuiOverlaysEvent overlays) {
        // FIXME overlays.registerAbove(VanillaGuiOverlay.DEBUG_SCREEN.id(), new ResourceLocation(Constants.MOD_ID, "room_meta_debug"), new RoomMetadataDebugOverlay());
    }
}
