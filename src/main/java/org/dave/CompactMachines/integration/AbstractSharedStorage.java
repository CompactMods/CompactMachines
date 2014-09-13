package org.dave.CompactMachines.integration;

import org.dave.CompactMachines.handler.SharedStorageHandler;

import net.minecraft.nbt.NBTTagCompound;

public abstract class AbstractSharedStorage {
	public final SharedStorageHandler storageHandler;
	
	public int coord;
	public int side;
	
	private boolean dirty;
	private int changeCount;
	
	public AbstractSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		this.storageHandler = storageHandler;
		this.coord = coord;
		this.side = side;
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
	
	public abstract NBTTagCompound saveToTag();
    
	public abstract void loadFromTag(NBTTagCompound tag);
	
}
