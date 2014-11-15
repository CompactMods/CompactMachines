package org.dave.CompactMachines.proxy;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.igw.IGWSupportNotifier;
import org.dave.CompactMachines.reference.Textures;

import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerHandlers() {
		FMLInterModComms.sendMessage("IGWMod", "org.dave.CompactMachines.igw.IGWHandler", "init");
		new IGWSupportNotifier();
	}

	@Override
	public void registerVillagerSkins() {
		VillagerRegistry.instance().registerVillagerSkin(ConfigurationHandler.villagerId, Textures.Entities.VILLAGER);
	}
}
