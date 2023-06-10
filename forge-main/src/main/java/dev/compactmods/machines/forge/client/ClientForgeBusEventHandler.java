package dev.compactmods.machines.forge.client;

import dev.compactmods.machines.api.core.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
