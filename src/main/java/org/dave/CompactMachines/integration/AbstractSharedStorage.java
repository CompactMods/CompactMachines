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
	public int hoppingMode;		// 0 - Off, 1 - To the inside, 2 - To the outside, 3 - Auto
	public boolean autoHopToInside;

	public int max_cooldown = 20;
	public int cooldown = 0;

	private boolean dirty;
	private int changeCount;

	public AbstractSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		this.storageHandler = storageHandler;
		this.coord = coord;
		this.side = side;
		this.hoppingMode = 0;
		this.autoHopToInside = false;
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
		autoHopToInside = compound.getBoolean("autoToInside");
	}

	public NBTTagCompound prepareTagCompound() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("hoppingMode", hoppingMode);
		compound.setBoolean("autoToInside", autoHopToInside);
		return compound;
	}

	public abstract NBTTagCompound saveToTag();

	public abstract void loadFromTag(NBTTagCompound tag);

	public abstract void hopToOutside(TileEntityMachine te, TileEntity outside);

	public abstract void hopToInside(TileEntityInterface te, TileEntity inside);

}
