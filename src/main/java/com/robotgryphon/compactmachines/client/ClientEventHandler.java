package com.robotgryphon.compactmachines.client;

import java.util.stream.Stream;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemColors(final ColorHandlerEvent.Item colors) {
        colors.getItemColors().register(new TunnelItemColor(), Registration.ITEM_TUNNEL.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final ColorHandlerEvent.Block colors) {
        colors.getBlockColors().register(new TunnelColors(), Registration.BLOCK_TUNNEL_WALL.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        RenderType cutout = RenderType.cutoutMipped();
        RenderTypeLookup.setRenderLayer(Registration.BLOCK_TUNNEL_WALL.get(), cutout);

        Stream.of(new int[] {}).map(Object::toString).toArray(String[]::new);
    }
}
