package org.dave.CompactMachines.proxy;

import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

import cpw.mods.fml.common.registry.GameRegistry;

public abstract class CommonProxy implements IProxy
{
	@Override
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityMachine.class, Names.Blocks.MACHINE);
		GameRegistry.registerTileEntity(TileEntityInterface.class, Names.Blocks.INTERFACE);
	}

	@Override
	public void registerHandlers() {}

	@Override
	public void registerVillagerSkins() {}

	@Override
	public void registerRenderers() {}

	@Override
	public boolean isClient() {
		return false;
	}
}
