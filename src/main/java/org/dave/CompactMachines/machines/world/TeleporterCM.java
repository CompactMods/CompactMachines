package org.dave.CompactMachines.machines.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class TeleporterCM extends Teleporter {

	public TeleporterCM(WorldServer ws) {
		super(ws);
	}

	@Override
	public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
	{}
}
