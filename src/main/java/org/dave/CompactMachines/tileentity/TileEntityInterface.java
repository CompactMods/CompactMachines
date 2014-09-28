package org.dave.CompactMachines.tileentity;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.api.ProjectRedAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.integration.appeng.AESharedStorage;
import org.dave.CompactMachines.integration.appeng.CMGridBlock;
import org.dave.CompactMachines.integration.bundledredstone.BRSharedStorage;
import org.dave.CompactMachines.integration.fluid.FluidSharedStorage;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.integration.opencomputers.OpenComputersSharedStorage;
import org.dave.CompactMachines.integration.redstoneflux.FluxSharedStorage;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Reference;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional;
@Optional.InterfaceList({
	@Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
	@Optional.Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Transmission"),
	@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers")
})
public class TileEntityInterface extends TileEntityCM implements IInventory, IFluidHandler, IEnergyHandler, IGridHost, IBundledTile, Environment {

	public FluidSharedStorage storageLiquid;
	public ItemSharedStorage storage;
	public FluxSharedStorage storageFlux;
	public AESharedStorage storageAE;
	public BRSharedStorage storageBR;
	public OpenComputersSharedStorage storageOC;

	public CMGridBlock gridBlock;

	public int coords;
	public int side;

	public int _fluidid;
	public int _fluidamount;
	public int _energy;
	public int _hoppingmode;

	public TileEntityInterface() {
		super();
		_fluidid = -1;
		_fluidamount = 0;
		_energy = 0;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if(Reference.OC_AVAILABLE && !worldObj.isRemote && storageOC != null) {
			Node node = storageOC.getNode();
			if(node != null) {
				node.remove();
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(Reference.OC_AVAILABLE && !worldObj.isRemote && storageOC != null) {
			Node node = storageOC.getNode();
			if(node != null) {
				node.remove();
			}
		}
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


    @Override
	public void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        side = tag.getInteger("side");
        coords = tag.getInteger("coords");
    }

    @Override
	public void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        tag.setInteger("side", side);
        tag.setInteger("coords", coords);
    }

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(Reference.PR_AVAILABLE) {
			updateIncomingSignals();
		}

		if(Reference.OC_AVAILABLE && !worldObj.isRemote) {
			Node node = storageOC.getNode();

			if(node != null && node.network() == null) {
				li.cil.oc.api.Network.joinOrCreateNetwork(this);
			}
		}

		if (!worldObj.isRemote)	{
			ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
			TileEntity tileEntityInside = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);

			if(tileEntityInside != null) {
				hopStorage(storage, tileEntityInside);
				hopStorage(storageLiquid, tileEntityInside);
				hopStorage(storageFlux, tileEntityInside);
			}
		}
	}

	private void updateIncomingSignals() {
		boolean needsNotify = false;
		boolean haveChanges = false;

		byte[] previous = storageBR.interfaceBundledSignal;
		byte[] current = ProjectRedAPI.transmissionAPI.getBundledInput(worldObj, xCoord, yCoord, zCoord, ForgeDirection.getOrientation(side).getOpposite().ordinal());
		if(current != null) {
			for(int i = 0; i < current.length; i++) {
				if(previous[i] != current[i]) {
					haveChanges = true;
					previous[i] = current[i];
				}
			}
		}

		if(haveChanges) {
			//LogHelper.info("Interface input on side " + ForgeDirection.getOrientation(side) + " is now: " + getByteString(previous));
			storageBR.machineNeedsNotify = true;
		}

		storageBR.setDirty();
	}

	private void hopStorage(AbstractSharedStorage storage, TileEntity tileEntityInside) {
		if(storage != null && (storage.hoppingMode == 1 || storage.hoppingMode == 3 && storage.autoHopToInside == true)) {
			storage.hopToInside(this, tileEntityInside);
		}
	}

	public void reloadStorage() {
		storage = (ItemSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "item");
		storageLiquid = (FluidSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "liquid");
		storageFlux = (FluxSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "flux");

		if(Reference.OC_AVAILABLE) {
			storageOC = (OpenComputersSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "OpenComputers");
		}

		if(Reference.AE_AVAILABLE) {
			storageAE = (AESharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "appeng");
		}

		if(Reference.PR_AVAILABLE) {
			storageBR = (BRSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(coords, side, "bundledRedstone");
		}
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
    public void setInventorySlotContents(int var1, ItemStack var2) {
    	storage.autoHopToInside = false;
    	storage.setDirty();
    	storage.setInventorySlotContents(var1, var2);
    }

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
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
    	storageLiquid.autoHopToInside = false;
    	storageLiquid.setDirty();
    	return storageLiquid.fill(from, resource, doFill);
    }

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
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		storageFlux.autoHopToInside = false;
		storageFlux.setDirty();
		return storageFlux.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) { return storageFlux.extractEnergy(maxExtract, simulate); }


	@Override
	public int getEnergyStored(ForgeDirection from) { return storageFlux.getEnergyStored(); }

	@Override
	public int getMaxEnergyStored(ForgeDirection from) { return storageFlux.getMaxEnergyStored(); }

	public int getHoppingMode(ForgeDirection from) { return storage.hoppingMode; }

	public CMGridBlock getGridBlock(ForgeDirection dir) {
		if(gridBlock == null) {
			gridBlock = new CMGridBlock(this);
		}

		return gridBlock;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		return storageAE.getInterfaceNode(getGridBlock(dir));
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public void securityBreak() { }

	@Override
	@Optional.Method(modid = "ProjRed|Transmission")
	public byte[] getBundledSignal(int dir) {
		byte[] current = storageBR.machineBundledSignal;

		if(current == null) {
			return null;
		}

		byte[] result = new byte[current.length];
		for(int i = 0; i < current.length; i++) {
			//Output = Opposite-Input unless Opposite-Output is made by us
			int a = current[i] & 255;
			int b = storageBR.machineOutputtedSignal[i] & 255;
			int c = a;
			if(b > 0) {
				continue;
			}

			result[i] = (byte)c;
		}

		storageBR.interfaceOutputtedSignal = result;
		storageBR.setDirty();

		//LogHelper.info("Interface outputting to " + ForgeDirection.getOrientation(dir) + ": " + getByteString(result));

		return result;
	}



	@Override
	@Optional.Method(modid = "ProjRed|Transmission")
	public boolean canConnectBundled(int side) {
		return true;
	}



	@Override
	@Optional.Method(modid = "OpenComputers")
	public Node node() {
		return storageOC.getNode();
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onConnect(Node node) {}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onDisconnect(Node node) {}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public void onMessage(Message message) {}


}
