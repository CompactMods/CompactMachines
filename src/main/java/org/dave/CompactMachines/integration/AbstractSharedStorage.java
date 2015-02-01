package org.dave.CompactMachines.integration;

import org.dave.CompactMachines.handler.SharedStorageHandler;

public abstract class AbstractSharedStorage {
	public final SharedStorageHandler	storageHandler;

	public int							coord;
	public int							side;

	public AbstractSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		this.storageHandler = storageHandler;
		this.coord = coord;
		this.side = side;
	}

	public abstract String type();

	public int getSide() {
		return this.side;
	}

	public int getCoord() {
		return this.coord;
	}

	public SharedStorageHandler getHandler() {
		return this.storageHandler;
	}

}
