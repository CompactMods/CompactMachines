package dev.compactmods.machines.client.room;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public interface RoomsClient {

   static void registerEvents(IEventBus modBus) {
	  modBus.addListener(RoomClientEvents::registerMenuScreens);
	  modBus.addListener(RoomClientEvents::onKeybindRegistration);
	  modBus.addListener(RoomClientEvents::onOverlayRegistration);

	  NeoForge.EVENT_BUS.addListener(RoomClientEvents::handleKeybinds);
   }
}
