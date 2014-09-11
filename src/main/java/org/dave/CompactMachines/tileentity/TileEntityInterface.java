package org.dave.CompactMachines.tileentity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.CompactMachines.handler.storage.FluidSharedStorage;
import org.dave.CompactMachines.handler.storage.FluxSharedStorage;
import org.dave.CompactMachines.handler.storage.ItemSharedStorage;
import org.dave.CompactMachines.handler.storage.SharedStorageHandler;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.utility.LogHelper;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;


public class TileEntityInterface extends TileEntityCM implements IInventory, IFluidHandler, IEnergyHandler {
	
	public FluidSharedStorage storageLiquid;
	public ItemSharedStorage storage;
	public FluxSharedStorage storageFlux;
	
	public int coords;
	public int side;
	
	public int _fluidid;
	public int _fluidamount;
	public int _energy;
	
	public TileEntityInterface() {
		super();
		_fluidid = -1;
		_fluidamount = 0;
		_energy = 0;
	}

	
	
    @Override
    public void validate()
    {
        super.validate();
        if(!(worldObj instanceof WorldServer) == worldObj.isRemote) {
            reloadStorage();
        }
    }	
	
	public void setSide(int side) {
		this.side = side;
		reloadStorage();
		markDirty();
	}
	
	public void setCoords(int coords) {
		this.coords = coords;
		reloadStorage();
		markDirty();		
	}
	
	public void setCoordSide(int coords, int side) {
		this.coords = coords;
		this.side = side;
		reloadStorage();
		markDirty();		
	}

	
    public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        side = tag.getInteger("side");
        coords = tag.getInteger("coords");
    }

    public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("side", side);
        tag.setInteger("coords", coords);
    }	
	
    
    
	public void reloadStorage() {
		storage = (ItemSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "item");
		storageLiquid = (FluidSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "liquid");
		storageFlux = (FluxSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "flux");
	}
	
    @Override
    public int getSizeInventory() { return storage.getSizeInventory(); }

    @Override
    public ItemStack getStackInSlot(int var1) { return storage.getStackInSlot(var1); }

    @Override
    public ItemStack decrStackSize(int var1, int var2) { return storage.decrStackSize(var1, var2); }

    @Override
    public ItemStack getStackInSlotOnClosing(int var1) { return storage.getStackInSlotOnClosing(var1); }

    @Override
    public void setInventorySlotContents(int var1, ItemStack var2) { storage.setInventorySlotContents(var1, var2); }

	@Override
	public String getInventoryName() {
		return this.hasCustomName() ? this.getCustomName() : Names.Containers.INTERFACE;
	}

	@Override
	public boolean hasCustomInventoryName() {
		return this.hasCustomName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}
	
    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack) {
        return true;
    }	
    
    
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) { return storageLiquid.fill(from, resource, doFill); }
    
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return storageLiquid.drain(from, maxDrain, doDrain); }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return storageLiquid.drain(from, resource, doDrain); }
    
    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) { return storageLiquid.canDrain(from, fluid); }
    
    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) { return storageLiquid.canFill(from, fluid); }
    
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) { return storageLiquid.getTankInfo(from); }    
    
    public FluidStack getFluid() {
    	return storageLiquid.getFluid();
    }

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) { return storageFlux.receiveEnergy(maxReceive, simulate); }

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) { return storageFlux.extractEnergy(maxExtract, simulate); }


	@Override
	public int getEnergyStored(ForgeDirection from) { return storageFlux.getEnergyStored(); }

	@Override
	public int getMaxEnergyStored(ForgeDirection from) { return storageFlux.getMaxEnergyStored(); }    
    
    
}
