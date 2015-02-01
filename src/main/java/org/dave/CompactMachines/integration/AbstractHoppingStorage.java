package org.dave.CompactMachines.integration;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.dave.CompactMachines.handler.SharedStorageHandler;

public abstract class AbstractHoppingStorage extends AbstractBufferedStorage {
	private int		hoppingMode;			// 0 - Off, 1 - To the inside, 2 - To the outside, 3 - Auto
	private boolean	autoHopToInside;

	protected int	max_cooldown	= 20;
	private int		cooldown		= 0;

	public AbstractHoppingStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		this.hoppingMode = 0;
		this.autoHopToInside = false;
	}

	public int getHoppingMode() {
		return this.hoppingMode;
	}

	public void setHoppingMode(int mode) {
		this.hoppingMode = mode;
	}

	public boolean isAutoHoppingToInside() {
		return autoHopToInside;
	}

	public void setAutoHoppingToInside(boolean state) {
		this.autoHopToInside = state;
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("hoppingMode", hoppingMode);
		compound.setBoolean("autoToInside", autoHopToInside);
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		hoppingMode = tag.getInteger("hoppingMode");
		autoHopToInside = tag.getBoolean("autoToInside");
	}

	public void hoppingTick(TileEntity target, boolean useOppositeSide) {
		if (cooldown == max_cooldown) {
			cooldown = 0;
		} else {
			cooldown++;
			return;
		}

		hopToTileEntity(target, useOppositeSide);
	}

	public abstract void hopToTileEntity(TileEntity target, boolean useOppositeSide);
}
