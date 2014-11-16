package org.dave.CompactMachines.tileentity;

import java.util.HashMap;

import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import mrtjp.projectred.api.IBundledTile;
import mrtjp.projectred.api.ProjectRedAPI;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.init.ModBlocks;
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

import appeng.api.movable.IMovableTile;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import cofh.api.energy.IEnergyHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@Optional.InterfaceList({
	@Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
	@Optional.Interface(iface = "appeng.api.movable.IMovableTile", modid = "appliedenergistics2"),
	@Optional.Interface(iface = "mrtjp.projectred.api.IBundledTile", modid = "ProjRed|Transmission"),
	@Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "OpenComputers"),
	@Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "Mekanism"),
	@Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "Mekanism")
})
public class TileEntityMachine extends TileEntityCM implements ISidedInventory, IFluidHandler, IGasHandler, ITubeConnection, IEnergyHandler, IGridHost, IMovableTile, IBundledTile, SidedEnvironment {

	public int coords = -1;
	public int[] _fluidid;
	public int[] _fluidamount;
	public int[] _gasid;
	public int[] _gasamount;
	public int[] _energy;
	public int meta = 0;

	public boolean isUpgraded = false;

	public HashMap<Integer, Vec3> interfaces;
	public HashMap<Integer, CMGridBlock> gridBlocks;
	public HashMap<Integer, IGridNode> gridNodes;
	private boolean	init;

	public static final int INVENTORY_SIZE = 6;

	public TileEntityMachine() {
		super();
		_fluidid = new int[6];
		_fluidamount = new int[6];
		_gasid = new int[6];
		_gasamount = new int[6];
		_energy = new int[6];


		gridBlocks = new HashMap<Integer, CMGridBlock>();
		gridNodes = new HashMap<Integer, IGridNode>();
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

	public GasSharedStorage getStorageGas(int side) {
		return (GasSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "gas");
	}

	public FluxSharedStorage getStorageFlux(int side) {
		return (FluxSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "flux");
	}

	public AESharedStorage getStorageAE(int side) {
		return (AESharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "appeng");
	}

	public BRSharedStorage getStorageBR(int side) {
		return (BRSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "bundledRedstone");
	}

	public OpenComputersSharedStorage getStorageOC(int side) {
		return (OpenComputersSharedStorage)SharedStorageHandler.instance(worldObj.isRemote).getStorage(this.coords, side, "OpenComputers");
	}


	@Override
	public void readFromNBT(NBTTagCompound nbtTagCompound)
	{
		super.readFromNBT(nbtTagCompound);

		//LogHelper.info("Reading nbt data of machine:");
		coords = nbtTagCompound.getInteger("coords");
		meta = nbtTagCompound.getInteger("meta");
		isUpgraded = nbtTagCompound.getBoolean("upgraded");

		if(isUpgraded && worldObj != null && worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		readInterfacesFromNBT(nbtTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTagCompound)
	{
		super.writeToNBT(nbtTagCompound);

		if (!nbtTagCompound.hasKey("meta")) {
			nbtTagCompound.setInteger("meta", meta);
		}

		//LogHelper.info("Writing nbt data");
		nbtTagCompound.setInteger("coords", coords);
		nbtTagCompound.setBoolean("upgraded", isUpgraded);

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
	@SideOnly(Side.SERVER)
	public void onChunkUnload() {
		super.onChunkUnload();

		if(worldObj.isRemote) {
			return;
		}

		if(ConfigurationHandler.chunkLoadingMode == 2) {
			CompactMachines.instance.machineHandler.disableMachine(this);
		}

		if(Reference.OC_AVAILABLE) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(getStorageOC(dir.ordinal()) == null) {
					continue;
				}

				OpenComputersSharedStorage storage = getStorageOC(dir.ordinal());
				Node node = storage.getNode();
				if(node != null) {
					node.remove();
				}
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();

		if(Reference.OC_AVAILABLE && !worldObj.isRemote) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(getStorageOC(dir.ordinal()) == null) {
					continue;
				}

				OpenComputersSharedStorage storage = getStorageOC(dir.ordinal());
				Node node = storage.getNode();
				if(node != null) {
					node.remove();
				}
			}
		}
	}

	public void initialize() {
		if(worldObj.isRemote) {
			return;
		}

		if(ConfigurationHandler.chunkLoadingMode != 0 && !CompactMachines.instance.machineHandler.isCoordChunkLoaded(this)) {
			CompactMachines.instance.machineHandler.forceChunkLoad(this.coords);
		}

		if(Reference.OC_AVAILABLE) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(getStorageOC(dir.ordinal()) == null) {
					continue;
				}

				OpenComputersSharedStorage storage = getStorageOC(dir.ordinal());
				Node node = storage.getNode();
				if(node != null && node.network() == null) {
					li.cil.oc.api.Network.joinOrCreateNetwork(this);
				}
			}
		}

	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!init && !isInvalid() && coords != -1) {
			initialize();
			init = true;
		}

		if(Reference.PR_AVAILABLE) {
			updateIncomingSignals();
		}

		if (!worldObj.isRemote)	{
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if(getStorage(dir.ordinal()) == null) {
					continue;
				}

				TileEntity outside = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
				if(outside != null) {
					hopStorage(getStorage(dir.ordinal()), outside);
					hopStorage(getStorageFluid(dir.ordinal()), outside);
					hopStorage(getStorageGas(dir.ordinal()), outside);
					hopStorage(getStorageFlux(dir.ordinal()), outside);
				}
			}
		}
	}

	private void hopStorage(AbstractSharedStorage storage, TileEntity outside) {
		if(storage != null && (storage.hoppingMode == 2 || storage.hoppingMode == 3 && storage.autoHopToInside == false)) {
			storage.hopToOutside(this, outside);
		}
	}


	@Override
	public int getSizeInventory() {
		return INVENTORY_SIZE;
	}

	@Override
	public ItemStack getStackInSlot(int slotIndex) {
		if(coords == -1) {
			return null;
		}
		return getStorage(slotIndex).getStackInSlot(0);
	}

	@Override
	public ItemStack decrStackSize(int slotIndex, int decreaseAmount) {
		if(coords == -1) {
			return null;
		}
		return getStorage(slotIndex).decrStackSize(0, decreaseAmount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotIndex) {
		if(coords == -1) {
			return null;
		}
		return getStorage(slotIndex).getStackInSlotOnClosing(0);
	}

	@Override
	public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
		if(coords == -1) {
			return;
		}
		ItemSharedStorage storage = getStorage(slotIndex);
		storage.autoHopToInside = true;
		storage.setDirty();
		storage.setInventorySlotContents(0, itemStack);
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
		if(coords == -1) {
			return 0;
		}
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
		if(coords == -1) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canInsertItem(int slotIndex, ItemStack itemStack, int side) {
		if(coords == -1) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canExtractItem(int slotIndex, ItemStack itemStack, int side) {
		if(coords == -1) {
			return false;
		}
		return true;
	}


    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		if(coords == -1) {
			return 0;
		}
    	FluidSharedStorage fss = getStorageFluid(from.ordinal());
    	if(doFill && resource.amount > 0) {
    		fss.autoHopToInside = true;
    		fss.setDirty();
    	}
    	return fss.fill(from, resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) { return getStorageFluid(from.ordinal()).drain(from, maxDrain, doDrain); }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) { return getStorageFluid(from.ordinal()).drain(from, resource, doDrain); }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
		if(coords == -1) {
			return false;
		}
    	return getStorageFluid(from.ordinal()).canDrain(from, fluid);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
		if(coords == -1) {
			return false;
		}
    	return getStorageFluid(from.ordinal()).canFill(from, fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) { return getStorageFluid(from.ordinal()).getTankInfo(from); }

    public FluidStack getFluid(int side) {
    	return getStorageFluid(side).getFluid();
    }


    public FluidStack getFluid(ForgeDirection from) {
    	return getStorageFluid(from.ordinal()).getFluid();
    }

    public int receiveGas(ForgeDirection from, GasStack stack) {
		if (coords == -1) {
			return 0;
		}

        GasSharedStorage gss = getStorageGas(from.ordinal());

        // XXX: Should we test with canReceiveGas first? Or do we rely on 
        // suppliers to do this?
        gss.autoHopToInside = true;
        gss.setDirty();

        return gss.receiveGas(from, stack);
    }

    public GasStack drawGas(ForgeDirection from, int amount) {
        return getStorageGas(from.ordinal()).drawGas(from, amount);
    }

    public boolean canReceiveGas(ForgeDirection from, Gas type) {
        return getStorageGas(from.ordinal()).canReceiveGas(from, type);
    }

    public boolean canDrawGas(ForgeDirection from, Gas type) {
        return getStorageGas(from.ordinal()).canDrawGas(from, type);
    }

    public boolean canTubeConnect(ForgeDirection side) {
        return true;
    }

    public GasStack getGasContents(ForgeDirection from) {
        return getStorageGas(from.ordinal()).getGasContents();
    }

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		if(coords == -1) {
			return 0;
		}
		FluxSharedStorage fss = getStorageFlux(from.ordinal());
		if(!simulate && maxReceive > 0) {
			fss.autoHopToInside = true;
			fss.setDirty();
		}
		return fss.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		if(coords == -1) {
			return 0;
		}
		return getStorageFlux(from.ordinal()).extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		if(coords == -1) {
			return 0;
		}
		return getStorageFlux(from.ordinal()).getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		if(coords == -1) {
			return 0;
		}
		return getStorageFlux(from.ordinal()).getMaxEnergyStored();
	}

	public CMGridBlock getGridBlock(ForgeDirection dir) {
		CMGridBlock gridBlock = gridBlocks.get(dir.ordinal());
		if(gridBlock == null) {
			gridBlock = new CMGridBlock(this);
			gridBlocks.put(dir.ordinal(), gridBlock);
		}

		return gridBlock;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if(!worldObj.isRemote) {
			IGridNode gridNode = gridNodes.get(dir.ordinal());
			if(gridNode == null) {
				gridNode = getStorageAE(dir.ordinal()).getMachineNode(getGridBlock(dir));
				gridNodes.put(dir.ordinal(), gridNode);
			}
			/*
			LogHelper.info("Gridnode " + dir + " is: " + gridNode.hashCode());
			for(IGridConnection conn : gridNode.getConnections()) {
				LogHelper.info(" * Connection between: " + conn.a().hashCode() + " and " + conn.b().hashCode());
			}
			*/

			return gridNode;
		}

		return null;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		return AECableType.DENSE;
	}

	@Optional.Method(modid = "appliedenergistics2")
	@Override
	public void securityBreak() { }

	public ItemStack getItemDrop() {
		ItemStack stack = new ItemStack(ModBlocks.machine, 1, meta);

		if(isUpgraded) {
			if(stack.stackTagCompound == null) {
				stack.stackTagCompound = new NBTTagCompound();
			}
			//LogHelper.info("Dropping item stack with coords: " + coords);
			stack.stackTagCompound.setInteger("coords", coords);
		}

		if(hasCustomName()) {
			stack.setStackDisplayName(getCustomName());
		}

		return stack;
	}

	public void dropAsItem() {
		ItemStack stack = getItemDrop();

		EntityItem entityitem = new EntityItem(this.getWorldObj(), this.xCoord, this.yCoord + 0.5F, this.zCoord, stack);

		entityitem.lifespan = 1200;
		entityitem.delayBeforeCanPickup = 10;

		float f3 = 0.05F;
		entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3;
		entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + 0.2F;
		entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3;
		this.getWorldObj().spawnEntityInWorld(entityitem);
	}

	@Override
	@Optional.Method(modid = "appliedenergistics2")
	public boolean prepareToMove() {
		if(isUpgraded) {
			return true;
		}
		return false;
	}

	@Override
	@Optional.Method(modid = "appliedenergistics2")
	public void doneMoving() { }


	@Optional.Method(modid = "ProjRed|Transmission")
	private void updateIncomingSignals() {
		boolean needsNotify = false;
		boolean haveChanges = false;
		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			BRSharedStorage br = getStorageBR(dir.ordinal());
			if(br.machineNeedsNotify) {
				//LogHelper.info("Signal into one of the interfaces changed: " + dir);
				needsNotify = true;
				br.machineNeedsNotify = false;
			}
			byte[] previous = br.machineBundledSignal;
			byte[] current = ProjectRedAPI.transmissionAPI.getBundledInput(worldObj, xCoord, yCoord, zCoord, dir.ordinal());

			if(current != null) {
				for(int i = 0; i < current.length; i++) {
					if(previous[i] != current[i]) {
						haveChanges = true;
						previous[i] = current[i];
					}
				}
			}
			br.setDirty();
		}

		if(haveChanges) {
			for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				Vec3 pos = interfaces.get(dir.ordinal());
				WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
				machineWorld.notifyBlockChange((int)pos.xCoord, (int)pos.yCoord, (int)pos.zCoord, ModBlocks.interfaceblock);
			}
		}

		if(needsNotify || haveChanges) {
			worldObj.notifyBlockChange(xCoord, yCoord, zCoord, blockType);
		}
	}

	@Override
	@Optional.Method(modid = "ProjRed|Transmission")
	public byte[] getBundledSignal(int dir) {

		BRSharedStorage storage = getStorageBR(dir);
		byte[] current = storage.interfaceBundledSignal;

		if(current == null) {
			return null;
		}

		byte[] result = new byte[current.length];
		for(int i = 0; i < current.length; i++) {
			//Machine-Output = Interface-Input unless Interface-Output is made by us
			int a = current[i] & 255;
			int b = storage.interfaceOutputtedSignal[i] & 255;
			int c = a;
			if(b > 0) {
				continue;
			}

			result[i] = (byte)c;
		}

		storage.machineOutputtedSignal = result;
		storage.setDirty();

		//LogHelper.info("Machine outputting to " + ForgeDirection.getOrientation(dir) + ": " + getByteString(result));

		return result;
	}

	@Override
	@Optional.Method(modid = "ProjRed|Transmission")
	public boolean canConnectBundled(int side) {
		return true;
	}


	@Override
	@Optional.Method(modid = "OpenComputers")
	public Node sidedNode(ForgeDirection side) {
		if (worldObj.isRemote)	{
			return null;
		}
		return getStorageOC(side.ordinal()).getNode();
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public boolean canConnect(ForgeDirection side) {
		return true;
	}
}
