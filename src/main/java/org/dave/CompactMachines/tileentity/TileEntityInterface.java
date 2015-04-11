package org.dave.CompactMachines.tileentity;

import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.api.ProjectRedAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.CompactMachines.api.IBlockProxy;
import org.dave.CompactMachines.handler.BlockProxyHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractHoppingStorage;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.integration.appeng.AESharedStorage;
import org.dave.CompactMachines.integration.appeng.CMGridBlock;
import org.dave.CompactMachines.integration.bundledredstone.BRSharedStorage;
import org.dave.CompactMachines.integration.fluid.FluidSharedStorage;
import org.dave.CompactMachines.integration.gas.GasSharedStorage;
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

import java.util.ArrayList;
import java.util.HashSet;

@Optional.InterfaceList({
		@Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
		@Optional.Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Transmission"),
		@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers"),
		@Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "Mekanism"),
		@Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "Mekanism")
})
public class TileEntityInterface extends TileEntityCM implements IInventory, IFluidHandler, IGasHandler, ITubeConnection, IEnergyHandler, IGridHost, IBundledTile, Environment, IBlockProxy {

	public CMGridBlock	gridBlock;

	public int			coords;
	public int			side;

	public int			_fluidid;
	public int			_fluidamount;
	public int			_gasid;
	public int			_gasamount;
	public int			_energy;
	public int			_hoppingmode;

	public TileEntityInterface() {
		super();
		_fluidid = -1;
		_fluidamount = 0;
		_gasid = -1;
		_gasamount = 0;
		_energy = 0;
	}

	@Override
	public BlockLocation[] getConnectedBlocks(ForgeDirection facing) {
		// TODO test
		ArrayList<BlockLocation> list = new ArrayList<BlockLocation>();
		HashSet<TileEntityMachine> set = BlockProxyHandler.getMS(coords);
		for (TileEntityMachine machine : set) {
			ForgeDirection dir = facing.getOpposite();
			list.add(new BlockLocation(machine.getWorldObj(), machine.xCoord + dir.offsetX, machine.yCoord + dir.offsetY, machine.zCoord + dir.offsetZ, facing));
		}
		return (BlockLocation[]) list.toArray();
	}

	public ItemSharedStorage getStorageItem() {
		return (ItemSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "item");
	}

	public FluidSharedStorage getStorageFluid() {
		return (FluidSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "liquid");
	}

	public GasSharedStorage getStorageGas() {
		return (GasSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "gas");
	}

	public FluxSharedStorage getStorageFlux() {
		return (FluxSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "flux");
	}

	public AESharedStorage getStorageAE() {
		// TODO: Check entangled instance etc
		return (AESharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "appeng");
	}

	public BRSharedStorage getStorageBR() {
		return (BRSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "bundledRedstone");
	}

	public OpenComputersSharedStorage getStorageOC() {
		return (OpenComputersSharedStorage) SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "OpenComputers");
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();

		if (Reference.OC_AVAILABLE && !worldObj.isRemote && getStorageOC() != null) {
			Node node = getStorageOC().getNode();
			if (node != null) {
				node.remove();
			}
		}
	}

	@Override
	public void validate() {
		super.validate();
		if (worldObj.isRemote) {
			return;
		}
		BlockProxyHandler.add(coords, this);
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if (Reference.OC_AVAILABLE && !worldObj.isRemote && getStorageOC() != null) {
			Node node = getStorageOC().getNode();
			if (node != null) {
				node.remove();
			}
		}

		if (worldObj.isRemote) {
			return;
		}
		BlockProxyHandler.remove(coords, this);
	}

	public void setSide(int side) {
		this.side = side;
		markDirty();
	}

	public void setCoords(int coords) {
		this.coords = coords;
		markDirty();
	}

	public void setCoordSide(int coords, int side) {
		this.coords = coords;
		this.side = side;
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

		if (Reference.PR_AVAILABLE) {
			updateIncomingSignals();
		}

		if (worldObj.isRemote) {
			return;
		}

		if (Reference.OC_AVAILABLE) {
			Node node = getStorageOC().getNode();

			if (node != null && node.network() == null) {
				li.cil.oc.api.Network.joinOrCreateNetwork(this);
			}
		}

		if (Reference.AE_AVAILABLE) {
			getGridNode(ForgeDirection.UNKNOWN);
		}

		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		TileEntity tileEntityInside = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);

		if (tileEntityInside != null) {
			for (AbstractSharedStorage storage : SharedStorageHandler.instance(false).getAllStorages(coords, side)) {
				if (!(storage instanceof AbstractHoppingStorage)) {
					continue;
				}

				hopStorage((AbstractHoppingStorage) storage, tileEntityInside);
			}
		}
	}

	private void updateIncomingSignals() {
		boolean needsNotify = false;
		boolean haveChanges = false;

		byte[] previous = getStorageBR().interfaceBundledSignal;
		byte[] current = ProjectRedAPI.transmissionAPI.getBundledInput(worldObj, xCoord, yCoord, zCoord, ForgeDirection.getOrientation(side).getOpposite().ordinal());
		if (current != null) {
			for (int i = 0; i < current.length; i++) {
				if (previous[i] != current[i]) {
					haveChanges = true;
					previous[i] = current[i];
				}
			}
		}

		if (haveChanges) {
			//LogHelper.info("Interface input on side " + ForgeDirection.getOrientation(side) + " is now: " + getByteString(previous));
			getStorageBR().machineNeedsNotify = true;
		}

		getStorageBR().setDirty();
	}

	private void hopStorage(AbstractHoppingStorage storage, TileEntity tileEntityInside) {
		if (storage != null && (storage.getHoppingMode() == 1 || storage.getHoppingMode() == 3 && storage.isAutoHoppingToInside() == true)) {
			storage.hopToTileEntity(tileEntityInside, false);
		}
	}

	@Override
	public int getSizeInventory() {
		return getStorageItem().getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		return getStorageItem().getStackInSlot(var1);
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		return getStorageItem().decrStackSize(var1, var2);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		return getStorageItem().getStackInSlotOnClosing(var1);
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		getStorageItem().setAutoHoppingToInside(false);
		getStorageItem().setDirty();
		getStorageItem().setInventorySlotContents(var1, var2);
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
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		getStorageFluid().setAutoHoppingToInside(false);
		getStorageFluid().setDirty();
		return getStorageFluid().fill(from, resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return getStorageFluid().drain(from, maxDrain, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return getStorageFluid().drain(from, resource, doDrain);
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return getStorageFluid().canDrain(from, fluid);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return getStorageFluid().canFill(from, fluid);
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return getStorageFluid().getTankInfo(from);
	}

	public FluidStack getFluid() {
		return getStorageFluid().getFluid();
	}

	@Override
	@Optional.Method(modid = "Mekanism")
	public int receiveGas(ForgeDirection from, GasStack stack, boolean doTransfer) {
		getStorageGas().setAutoHoppingToInside(false);
		getStorageGas().setDirty();

		return getStorageGas().receiveGas(from, stack, doTransfer);
	}

	@Override
	@Optional.Method(modid = "Mekanism")
	public GasStack drawGas(ForgeDirection from, int amount, boolean doTransfer) {
		return getStorageGas().drawGas(from, amount, doTransfer);
	}

	@Override
	@Optional.Method(modid = "Mekanism")
	public boolean canReceiveGas(ForgeDirection from, Gas type) {
		return getStorageGas().canReceiveGas(from, type);
	}

	@Override
	@Optional.Method(modid = "Mekanism")
	public boolean canDrawGas(ForgeDirection from, Gas type) {
		return getStorageGas().canDrawGas(from, type);
	}

	@Optional.Method(modid = "Mekanism")
	public GasStack getGasContents() {
		return getStorageGas().getGasContents();
	}

	@Override
	@Optional.Method(modid = "Mekanism")
	public boolean canTubeConnect(ForgeDirection side) {
		return true;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		getStorageFlux().setAutoHoppingToInside(false);
		getStorageFlux().setDirty();
		return getStorageFlux().receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return getStorageFlux().extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return getStorageFlux().getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return getStorageFlux().getMaxEnergyStored();
	}

	public int getHoppingMode(ForgeDirection from) {
		return getStorageFlux().getHoppingMode();
	}

	public CMGridBlock getGridBlock(ForgeDirection dir) {
		if (gridBlock == null) {
			gridBlock = new CMGridBlock(this);
		}

		return gridBlock;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (worldObj.isRemote) {
			return null;
		}

		return getStorageAE().getInterfaceNode(getGridBlock(dir));
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public void securityBreak() {}

	@Override
	@Optional.Method(modid = "ProjRed|Transmission")
	public byte[] getBundledSignal(int dir) {
		byte[] current = getStorageBR().machineBundledSignal;

		if (current == null) {
			return null;
		}

		byte[] result = new byte[current.length];
		for (int i = 0; i < current.length; i++) {
			//Output = Opposite-Input unless Opposite-Output is made by us
			int a = current[i] & 255;
			int b = getStorageBR().machineOutputtedSignal[i] & 255;
			int c = a;
			if (b > 0) {
				continue;
			}

			result[i] = (byte) c;
		}

		getStorageBR().interfaceOutputtedSignal = result;
		getStorageBR().setDirty();

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
		return getStorageOC().getNode();
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
