package com.robotgryphon.compactmachines.core;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.client.TunnelColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onColors(final ColorHandlerEvent.Block colors) {
        colors.getBlockColors().register(new TunnelColors(), Registrations.BLOCK_TUNNEL_WALL.get());
    }
}
