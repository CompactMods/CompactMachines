package org.dave.CompactMachines.handler;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.machinedimension.MachineHandler;
import org.dave.CompactMachines.utility.LogHelper;

import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CMEventHandler {


	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event)
	{
		if (!event.world.isRemote && event.world.provider.dimensionId == ConfigurationHandler.dimensionId)
		{
			LogHelper.info("Loading machine world!");
			MachineHandler machineHandler = (MachineHandler) event.world.mapStorage.loadData(MachineHandler.class, "MachineHandler");
			if (machineHandler == null)	{
				machineHandler = new MachineHandler(event.world);
				machineHandler.markDirty();
			}

			machineHandler.setWorld(event.world);

			event.world.mapStorage.setData("MachineHandler", machineHandler);
			CompactMachines.instance.machineHandler = machineHandler;
		}
	}


}
