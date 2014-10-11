package org.dave.CompactMachines.handler;

import org.dave.CompactMachines.CompactMachines;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.relauncher.Side;

public class CMTickHandler {

	@SubscribeEvent
	public void tick(TickEvent event) {
		if(event.side == Side.SERVER && event.phase == Phase.START) {
			if(CompactMachines.instance.machineHandler != null) {
				CompactMachines.instance.machineHandler.tick();
			}
		}
	}
}
