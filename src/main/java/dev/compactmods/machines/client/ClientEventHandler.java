package dev.compactmods.machines.client;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Tunnels;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemColors(final ColorHandlerEvent.Item colors) {
        colors.getItemColors().register(new TunnelItemColor(), Tunnels.ITEM_TUNNEL.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final ColorHandlerEvent.Block colors) {
        // colors.getBlockColors().register(new TunnelColors(), Registration.BLOCK_TUNNEL_WALL.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        RenderType cutout = RenderType.cutoutMipped();
        // ItemBlockRenderTypes.setRenderLayer(Registration.BLOCK_TUNNEL_WALL.get(), cutout);
    }
}
