package org.dave.CompactMachines.proxy;

import cpw.mods.fml.common.event.FMLInterModComms;

public class ClientProxy extends CommonProxy
{
	@Override
	public void registerHandlers() {
		FMLInterModComms.sendMessage("IGWMod", "org.dave.CompactMachines.igw.IGWHandler", "init");
	}
}
