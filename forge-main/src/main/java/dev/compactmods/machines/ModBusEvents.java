package dev.compactmods.machines;

import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.network.CompactMachinesNet;
import dev.compactmods.machines.network.RoomNetworkHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        var logger = LoggingUtil.modLog();

        logger.trace("Initializing network handler.");
        CompactMachinesNet.setupMessages();
        RoomNetworkHandler.setupMessages();

        logger.trace("Registering advancement triggers.");
        AdvancementTriggers.init();
    }
}
