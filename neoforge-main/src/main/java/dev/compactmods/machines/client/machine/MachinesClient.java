package dev.compactmods.machines.client.machine;

import net.neoforged.bus.api.IEventBus;

public interface MachinesClient {
   static void registerEvents(IEventBus modBus) {
	  modBus.addListener(MachineColors::onBlockColors);
	  modBus.addListener(MachineColors::onItemColors);
   }
}
