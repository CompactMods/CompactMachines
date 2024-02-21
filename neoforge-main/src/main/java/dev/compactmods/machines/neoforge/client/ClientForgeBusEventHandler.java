package dev.compactmods.machines.neoforge.client;

import dev.compactmods.machines.api.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeBusEventHandler {
    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent clientTick) {
        if(clientTick.phase != TickEvent.Phase.END)
            return;

        if(RoomExitKeyMapping.MAPPING.consumeClick())
            RoomExitKeyMapping.handle();
    }
}
