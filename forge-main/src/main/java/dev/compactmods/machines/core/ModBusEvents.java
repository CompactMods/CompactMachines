package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.room.network.RoomNetworkHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event) {
        CompactMachines.LOGGER.trace("Initializing network handler.");
        CompactMachinesNet.setupMessages();
        RoomNetworkHandler.setupMessages();

        CompactMachines.LOGGER.trace("Registering advancement triggers.");
        AdvancementTriggers.init();
    }
}
