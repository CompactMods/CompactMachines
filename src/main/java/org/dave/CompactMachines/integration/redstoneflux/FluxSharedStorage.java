package org.dave.CompactMachines.integration.redstoneflux;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;

public class FluxSharedStorage extends AbstractSharedStorage implements IEnergyStorage {

	protected int	energy		= 0;
	protected int	capacity	= 10000;
	protected int	maxReceive	= 10000;
	protected int	maxExtract	= 10000;

	public FluxSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		max_cooldown = ConfigurationHandler.cooldownRF;
	}

	@Override
	public String type() {
		return "flux";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = prepareTagCompound();
		compound.setInteger("Energy", energy);
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		loadHoppingModeFromCompound(tag);
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

	@Override
	public void hopToTileEntity(TileEntity tileEntity, boolean opposite) {
		if (getEnergyStored() == 0) {
			return;
		}

		if (cooldown == max_cooldown) {
			cooldown = 0;
		} else {
			cooldown++;
			return;
		}

		if (tileEntity instanceof IEnergyStorage) {
			//LogHelper.info("Hopping flux into IEnergyStorage");
			IEnergyStorage storage = (IEnergyStorage) tileEntity;

			int filled = storage.receiveEnergy(getEnergyStored(), false);
			if (filled > 0) {
				//LogHelper.info("Transferred RF: " + filled);
				this.extractEnergy(filled, false);
				tileEntity.markDirty();
			}

		} else if (tileEntity instanceof IEnergyHandler) {
			//LogHelper.info("Hopping flux into IEnergyHandler");
			IEnergyHandler handler = (IEnergyHandler) tileEntity;

			ForgeDirection hoppingSide = ForgeDirection.getOrientation(side);
			if (opposite) {
				hoppingSide = hoppingSide.getOpposite();
			}

			int filled = handler.receiveEnergy(hoppingSide, getEnergyStored(), false);
			if (filled > 0) {
				//LogHelper.info("Transferred RF: " + filled);
				this.extractEnergy(filled, false);
				tileEntity.markDirty();
			}
		}
	}

	@Override
	public boolean isHopping() {
		return true;
	}

}
