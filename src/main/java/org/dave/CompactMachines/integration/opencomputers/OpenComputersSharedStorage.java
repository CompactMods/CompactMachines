package org.dave.CompactMachines.integration.opencomputers;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

public class OpenComputersSharedStorage extends AbstractSharedStorage implements Environment {

	public Node node;

	public OpenComputersSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		this.side = side;
		this.coord = coord;
	}

	public Node getNode() {
		if(node == null) {
			//LogHelper.info("Creating new node for side: " + ForgeDirection.getOrientation(side));
			node = li.cil.oc.api.Network.newNode(this, Visibility.Network).withConnector().create();
			setDirty();
		}
		return node;
	}


	@Override
	public String type() {
		return "OpenComputers";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = prepareTagCompound();
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {

	}

	@Override
	public void hopToOutside(TileEntityMachine te, TileEntity outside) { }

	@Override
	public void hopToInside(TileEntityInterface te, TileEntity inside) { }

	@Override
	public Node node() {
		return getNode();
	}

	@Override
	public void onConnect(Node node) {
	}

	@Override
	public void onDisconnect(Node node) {
	}

	@Override
	public void onMessage(Message message) {
	}

}
