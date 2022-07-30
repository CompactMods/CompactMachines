package dev.compactmods.machines.client;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.core.UIRegistration;
import dev.compactmods.machines.room.client.MachineRoomScreen;
import dev.compactmods.machines.tunnel.client.TunnelColors;
import dev.compactmods.machines.tunnel.client.TunnelItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item colors) {
        colors.register(new TunnelItemColor(), Tunnels.ITEM_TUNNEL.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(new TunnelColors(), Tunnels.BLOCK_TUNNEL_WALL.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        RenderType cutout = RenderType.cutoutMipped();
        ItemBlockRenderTypes.setRenderLayer(Tunnels.BLOCK_TUNNEL_WALL.get(), cutout);

        MenuScreens.register(UIRegistration.MACHINE_MENU.get(), MachineRoomScreen::new);
    }
}
