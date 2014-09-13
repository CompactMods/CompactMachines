package org.dave.CompactMachines.integration.redstoneflux;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;

import cofh.api.energy.IEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;

public class FluxSharedStorage extends AbstractSharedStorage implements IEnergyStorage {

	protected int energy = 0;
	protected int capacity = 10000;
	protected int maxReceive = 10000;
	protected int maxExtract = 10000;	

	public FluxSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);
	}
	
	@Override
	public String type() {
		return "flux";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("Energy", energy);

        return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		energy = tag.getInteger("Energy");

	}

	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

		if (!simulate) {
			energy += energyReceived;
			setDirty();
		}
		return energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

		if (!simulate) {
			energy -= energyExtracted;
			setDirty();
		}
		return energyExtracted;
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return capacity;
	}

}
