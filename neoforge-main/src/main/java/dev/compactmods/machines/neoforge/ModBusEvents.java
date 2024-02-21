package dev.compactmods.machines.neoforge;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.network.RoomNetworkHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        var logger = LoggingUtil.modLog();

        logger.trace("Initializing network handler.");
        RoomNetworkHandler.setupMessages();
    }
}
