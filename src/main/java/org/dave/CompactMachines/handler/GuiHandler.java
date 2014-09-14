package org.dave.CompactMachines.handler;

import org.dave.CompactMachines.client.gui.inventory.GuiInterface;
import org.dave.CompactMachines.client.gui.inventory.GuiMachine;
import org.dave.CompactMachines.inventory.ContainerInterface;
import org.dave.CompactMachines.inventory.ContainerMachine;
import org.dave.CompactMachines.reference.GuiId;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public GuiHandler() {

	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,	int x, int y, int z) {
		if(ID == GuiId.INTERFACE.ordinal()) {
			TileEntityInterface tileEntityInterface = (TileEntityInterface)world.getTileEntity(x,y,z);
			return new ContainerInterface(player.inventory, tileEntityInterface);
		} else if(ID == GuiId.MACHINE.ordinal()) {
			TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(x,y,z);
			return new ContainerMachine(player.inventory, tileEntityMachine);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if(ID == GuiId.INTERFACE.ordinal()) {
			TileEntityInterface tileEntityInterface = (TileEntityInterface)world.getTileEntity(x,y,z);
			return new GuiInterface(player.inventory, tileEntityInterface);
		} else if(ID == GuiId.MACHINE.ordinal()) {
			TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(x,y,z);
			return new GuiMachine(player.inventory, tileEntityMachine);
		}
		return null;
	}

}
