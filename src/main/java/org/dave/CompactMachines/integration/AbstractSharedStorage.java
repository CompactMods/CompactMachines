package org.dave.CompactMachines.integration;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;


public abstract class AbstractSharedStorage {
	public final SharedStorageHandler storageHandler;

	public int coord;
	public int side;
	public int hoppingMode;		// 0 - Off, 1 - To the inside, 2 - To the outside

	// TODO: Make cooldown configurable
	public int max_cooldown = 20;
	public int cooldown = max_cooldown;

	private boolean dirty;
	private int changeCount;

	public AbstractSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		this.storageHandler = storageHandler;
		this.coord = coord;
		this.side = side;
		this.hoppingMode = 0;
	}

	public void setDirty() {
		if(storageHandler.client) {
			return;
		}

		if(!dirty) {
			dirty = true;
			storageHandler.requestSave(this);
		}

		changeCount++;
	}

	public void setClean() {
		dirty = false;
	}

	public int getChangeCount() {
		return changeCount;
	}

	public abstract String type();

	public void loadHoppingModeFromCompound(NBTTagCompound compound) {
		hoppingMode = compound.getInteger("hoppingMode");
	}

	public NBTTagCompound prepareTagCompound() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("hoppingMode", hoppingMode);
		return compound;
	}

	public abstract NBTTagCompound saveToTag();

	public abstract void loadFromTag(NBTTagCompound tag);

	public abstract void hopToOutside(TileEntityMachine te, TileEntity outside);

	public abstract void hopToInside(TileEntityInterface te, TileEntity inside);

}
