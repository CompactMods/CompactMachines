package org.dave.compactmachines3.misc;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.gui.machine.GuiMachineChunkHolder;


public class RenderTickCounter {
    public static int renderTicks = 0;

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if(event.phase == TickEvent.RenderTickEvent.Phase.START) {
            renderTicks++;
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onGuiClose(GuiOpenEvent event) {
        if(event.getGui() == null) {
            GuiMachineChunkHolder.rawData = null;
            GuiMachineChunkHolder.chunk = null;
        }
    }
}
