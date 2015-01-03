package org.dave.CompactMachines.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.machinedimension.MachineHandler;
import org.dave.CompactMachines.utility.LogHelper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class CMEventHandler {

	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event)
	{
		if (!event.world.isRemote && event.world.provider.dimensionId == ConfigurationHandler.dimensionId)
		{
			LogHelper.info("Loading machine world!");
			MachineHandler machineHandler = (MachineHandler) event.world.mapStorage.loadData(MachineHandler.class, "MachineHandler");
			if (machineHandler == null) {
				machineHandler = new MachineHandler(event.world);
				machineHandler.markDirty();
			}

			machineHandler.setWorld(event.world);

			event.world.mapStorage.setData("MachineHandler", machineHandler);
			CompactMachines.instance.machineHandler = machineHandler;
		}
	}

	@SubscribeEvent
	public void entityJoinWorldEvent(EntityJoinWorldEvent event) {
		if(event.world.isRemote) {
			return;
		}

		if (event.entity == null || !(event.entity instanceof EntityPlayer)) {
			return;
		}

		if(event.world.provider.dimensionId != ConfigurationHandler.dimensionId) {
			return;
		}

		if(ConfigurationHandler.allowEnterWithoutPSD) {
			return;
		}

		EntityPlayer player = (EntityPlayer)event.entity;

		NBTTagCompound playerNBT = player.getEntityData();
		if(!playerNBT.getBoolean("isUsingPSD")) {
			player.addPotionEffect(new PotionEffect(Potion.wither.id, 300, 2, false));	// Wither
			player.addPotionEffect(new PotionEffect(Potion.confusion.id, 300, 5, false)); // Nausea
		} else {
			playerNBT.removeTag("isUsingPSD");
		}

	}

}
