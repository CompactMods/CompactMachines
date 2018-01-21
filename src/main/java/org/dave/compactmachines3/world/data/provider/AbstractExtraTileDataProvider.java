package org.dave.compactmachines3.world.data.provider;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public abstract class AbstractExtraTileDataProvider {
    public abstract boolean worksWith(TileEntity te);
    public abstract NBTTagCompound writeExtraData(TileEntity tileEntity);
    public abstract void readExtraData(TileEntity tileEntity, NBTTagCompound data);
    public abstract String getName();
}
