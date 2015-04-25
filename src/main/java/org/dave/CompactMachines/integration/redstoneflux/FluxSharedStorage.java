package org.dave.CompactMachines.integration.redstoneflux;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractHoppingStorage;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;

public class FluxSharedStorage extends AbstractHoppingStorage implements IEnergyStorage {

	protected int	energy		= 0;

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
		NBTTagCompound compound = super.saveToTag();
		compound.setInteger("Energy", energy);
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		super.loadFromTag(tag);
		energy = tag.getInteger("Energy");
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int energyReceived = Math.min(ConfigurationHandler.capacityRF - energy, Math.min(ConfigurationHandler.capacityRF, maxReceive));

		if (!simulate) {
			energy += energyReceived;
			setDirty();
		}
		return energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int energyExtracted = Math.min(energy, Math.min(ConfigurationHandler.capacityRF, maxExtract));

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
		return ConfigurationHandler.capacityRF;
	}

	@Override
	public void hopToTileEntity(TileEntity tileEntity, boolean opposite) {
		if (getEnergyStored() == 0) {
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
}
