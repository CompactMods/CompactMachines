package org.dave.CompactMachines.integration.pneumaticcraft;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractBufferedStorage;

import pneumaticCraft.api.tileentity.AirHandlerSupplier;
import pneumaticCraft.api.tileentity.IAirHandler;
import pneumaticCraft.api.tileentity.IPneumaticMachine;
import pneumaticCraft.api.tileentity.ISidedPneumaticMachine;

@SuppressWarnings("deprecation")
public class PneumaticCraftSharedStorage extends AbstractBufferedStorage {
	public int			coord;
	public int			side;
	
	private IAirHandler interfaceAirHandler;
	private boolean 	interfaceValid = false;

	private Map<Integer, IAirHandler> connectedHandlers = new HashMap<Integer, IAirHandler>();

	public PneumaticCraftSharedStorage(SharedStorageHandler storageHandler,	int coord, int side) {
		super(storageHandler, coord, side);

		this.side = side;
		this.coord = coord;

		interfaceAirHandler = AirHandlerSupplier.getAirHandler(30, 50, 1000);
	}

	@Override
	public String type() {
		return "PneumaticCraft";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound tag = new NBTTagCompound();
		interfaceAirHandler.writeToNBTI(tag);
		return tag;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		if (tag.hasNoTags()) {
			return;
		}
		interfaceAirHandler.readFromNBTI(tag);
	}

	public void validateInterfaceAirHandler(TileEntity tile) {
		if (tile == null || tile.getWorldObj() == null) {
			return;
		}
		interfaceAirHandler.validateI(tile);
		interfaceValid = true;
		connectAll();
		setDirty();
	}

	public void invalidateInterfaceAirHandler() {
		disconnectAll();
		interfaceValid = false;
	}

	public IAirHandler getInterfaceAirHandler() {
		if (!isInterfaceValid()) {
			return null;
		}
		setDirty();
		return interfaceAirHandler;
	}

	public boolean isInterfaceValid() {
		return interfaceValid;
	}

	public Object createDummyAirHandler(TileEntity tile) {
		if (tile == null || tile.getWorldObj() == null) {
			return null;
		}
		IAirHandler dummyAirHandler = AirHandlerSupplier.getAirHandler(30, 50, 1);
		dummyAirHandler.validateI(tile);
		return dummyAirHandler;
	}

	public void connectAirHandler(int entangledInstance, TileEntity tile) {
		if (tile == null || tile.getWorldObj() == null) {
			return;
		}
		IAirHandler existHandler = connectedHandlers.get(entangledInstance);
		IAirHandler newHandler = getHandlerFromTile(tile);
		if (existHandler != newHandler) {
			removeAirHandler(entangledInstance);
			if (newHandler != null) {
				connectedHandlers.put(entangledInstance, newHandler);
				if (isInterfaceValid()) {
					interfaceAirHandler.createConnection(newHandler);
				}
			}
		}
	}

	public void removeAirHandler(int entangledInstance) {
		if (connectedHandlers.containsKey(entangledInstance)) {
			if (isInterfaceValid()) {
				interfaceAirHandler.removeConnection(connectedHandlers.get(entangledInstance));
			}
			connectedHandlers.remove(entangledInstance);
		}
	}

	private IAirHandler getHandlerFromTile(TileEntity tile) {
		IAirHandler handler = null;
        if(tile instanceof IPneumaticMachine && ((IPneumaticMachine)tile).isConnectedTo(ForgeDirection.getOrientation(side).getOpposite())) {
        	handler = ((IPneumaticMachine)tile).getAirHandler();
        } else if(tile instanceof ISidedPneumaticMachine) {
            handler = ((ISidedPneumaticMachine)tile).getAirHandler(ForgeDirection.getOrientation(side).getOpposite());
        }

		return handler;
	}

	private void connectAll() {
		if (isInterfaceValid()) {
			for (IAirHandler handler : connectedHandlers.values()) {
				interfaceAirHandler.createConnection(handler);
			}
		}
	}

	private void disconnectAll() {
		if (isInterfaceValid()) {
			for (IAirHandler handler : connectedHandlers.values()) {
				interfaceAirHandler.removeConnection(handler);
			}
		}
		connectedHandlers.clear();
	}
}
