package org.dave.CompactMachines.tileentity;

import java.util.HashMap;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.integration.fluid.FluidSharedStorage;
import org.dave.CompactMachines.integration.redstoneflux.FluxSharedStorage;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.utility.LogHelper;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileEntityMachine extends TileEntityCM implements ISidedInventory, IFluidHandler, IEnergyHandler {

	public int coords = -1;
	public int[] _fluidid;
	public int[] _fluidamount;
	public int[] _energy;
	
	public HashMap<Integer, Vec3> interfaces;
		
	public static final int INVENTORY_SIZE = 6;

	public TileEntityMachine() {
		super();
		_fluidid = new int[6];
		_fluidamount = new int[6];
		_energy = new int[6];
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side) {
		// Side directly determines the inventory
		return new int[] { side };
	}

	public ItemSharedStorage getStorage(int side) {
		return (ItemSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "item");
	}
	
	public FluidSharedStorage getStorageFluid(int side) {
		return (FluidSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "liquid");
	}
	
	public FluxSharedStorage getStorageFlux(int side) {
		return (FluxSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "flux");
	}	


	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound)
	{
		super.readFromNBT(nbtTagCompound);

        coords = nbtTagCompound.getInteger("coords");		
		
		readInterfacesFromNBT(nbtTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound)
	{
		super.writeToNBT(nbtTagCompound);
			
		if (!nbtTagCompound.hasKey("coords")) {
			nbtTagCompound.setInteger("coords", coords);
		}
		
		addInterfacesToNBT(nbtTagCompound);
	}

	private void readInterfaceFromNBT(NBTTagCompound nbt, String key, int direction) {
		if(nbt.hasKey(key)) {
			if(interfaces == null) {
				interfaces = new HashMap<Integer, Vec3>();
			}
			int[] xyz = nbt.getIntArray(key);
			Vec3 pos = Vec3.createVectorHelper(xyz[0], xyz[1], xyz[2]);
			interfaces.put(direction, pos);
		}
	}
	
	private void readInterfacesFromNBT(NBTTagCompound nbt) {
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_DOWN,  ForgeDirection.DOWN.ordinal());
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_UP,    ForgeDirection.UP.ordinal());
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_EAST,  ForgeDirection.EAST.ordinal());
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_WEST,  ForgeDirection.WEST.ordinal());
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_NORTH, ForgeDirection.NORTH.ordinal());
		readInterfaceFromNBT(nbt, Names.NBT.INTERFACE_SOUTH, ForgeDirection.SOUTH.ordinal());			
	}
		
	private void addInterfaceToNBT(NBTTagCompound nbt, String key, int direction) {
		if (!nbt.hasKey(key) && interfaces != null) {
			Vec3 pos = interfaces.get(direction);
			int x = (int)pos.xCoord;
			int y = (int)pos.yCoord;
			int z = (int)pos.zCoord;
			
			nbt.setIntArray(key, new int[]{x,y,z});			
		}		
	}
	
	private void addInterfacesToNBT(NBTTagCompound nbt) {
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_DOWN,  ForgeDirection.DOWN.ordinal());
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_UP,    ForgeDirection.UP.ordinal());
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_EAST,  ForgeDirection.EAST.ordinal());
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_WEST,  ForgeDirection.WEST.ordinal());
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_NORTH, ForgeDirection.NORTH.ordinal());
		addInterfaceToNBT(nbt, Names.NBT.INTERFACE_SOUTH, ForgeDirection.SOUTH.ordinal());		
	}
	
	@Override
	public int getSizeInventory() {
		return INVENTORY_SIZE;
	}
	
	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		return getStorage(slotIndex).getStackInSlot(0);
	}

	@Override
	public ItemStack decrStackSize(int slotIndex, int decreaseAmount) {
		return getStorage(slotIndex).decrStackSize(0, decreaseAmount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		return getStorage(slotIndex).getStackInSlotOnClosing(0);
	}

	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		getStorage(slotIndex).setInventorySlotContents(0, itemStack);
	}

	@Override
	public String getInventoryName() {
		return this.hasCustomName() ? this.getCustomName() : Names.Containers.MACHINE;
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
	public boolean isUseableByPlayer(EntityPlayer p_70300_1_) {
		return false;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return true;
	}
	
	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side) {
		return true;
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side) {
		return true;
	}
	
	
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) { return getStorageFluid(from.ordinal()).fill(from, resource, doFill); }
    
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return getStorageFluid(from.ordinal()).drain(from, maxDrain, doDrain); }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return getStorageFluid(from.ordinal()).drain(from, resource, doDrain); }
    
    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) { return getStorageFluid(from.ordinal()).canDrain(from, fluid); }
    
    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) { return getStorageFluid(from.ordinal()).canFill(from, fluid); }
    
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) { return getStorageFluid(from.ordinal()).getTankInfo(from); }    

    public FluidStack getFluid(int side) {
    	return getStorageFluid(side).getFluid();
    }
    
    
    public FluidStack getFluid(ForgeDirection from) {
    	return getStorageFluid(from.ordinal()).getFluid();
    }

    
	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return getStorageFlux(from.ordinal()).receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return getStorageFlux(from.ordinal()).extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return getStorageFlux(from.ordinal()).getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return getStorageFlux(from.ordinal()).getMaxEnergyStored();
	}


	
	
}
