package org.dave.compactmachines3.misc;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class RenderTickCounter {
    public static int renderTicks = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.RenderTickEvent.Phase.START) {
            renderTicks++;
        }
    }
}
