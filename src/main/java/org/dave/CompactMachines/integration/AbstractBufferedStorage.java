package org.dave.CompactMachines.integration;

import net.minecraft.nbt.NBTTagCompound;

import org.dave.CompactMachines.handler.SharedStorageHandler;

public abstract class AbstractBufferedStorage extends AbstractSharedStorage {
	private boolean	dirty;
	private int		changeCount;

	public AbstractBufferedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);
	}

	public void setDirty() {
		if (storageHandler.client) {
			return;
		}

		if (!dirty) {
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

	public abstract NBTTagCompound saveToTag();

	public abstract void loadFromTag(NBTTagCompound tag);
}
