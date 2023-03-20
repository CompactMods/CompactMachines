package dev.compactmods.machines.forge.client;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.forge.compat.curios.CuriosCompat;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.room.ui.MachineRoomScreen;
import dev.compactmods.machines.forge.room.ui.RoomUserInterfaceRegistration;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.tunnel.client.TunnelColors;
import dev.compactmods.machines.machine.client.MachineColors;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onItemColors(final RegisterColorHandlersEvent.Item colors) {
        colors.register(TunnelColors.ITEM, Tunnels.ITEM_TUNNEL.get());
        colors.register(MachineColors.ITEM, Machines.BOUND_MACHINE_BLOCK_ITEM.get());
        colors.register(MachineColors.ITEM, Machines.UNBOUND_MACHINE_BLOCK_ITEM.get());
    }

    @SubscribeEvent
    public static void onBlockColors(final RegisterColorHandlersEvent.Block colors) {
        colors.register(TunnelColors.BLOCK, Tunnels.BLOCK_TUNNEL_WALL.get());
        colors.register(MachineColors.BLOCK, Machines.MACHINE_BLOCK.get());
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent client) {
        MenuScreens.register(RoomUserInterfaceRegistration.MACHINE_MENU.get(), MachineRoomScreen::new);
    }

    @SubscribeEvent
    public static void onTextureStitch(final TextureStitchEvent.Pre stitch) {
        if(ModList.get().isLoaded("curios"))
            CuriosCompat.addTextures(stitch);
    }
}
