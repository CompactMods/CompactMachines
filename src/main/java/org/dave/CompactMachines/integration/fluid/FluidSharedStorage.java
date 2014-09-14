package org.dave.CompactMachines.integration.fluid;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.utility.FluidUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidSharedStorage extends AbstractSharedStorage implements IFluidHandler  {

    private class Tank extends ExtendedFluidTank
    {
        public Tank(int capacity)
        {
            super(capacity);
        }

        @Override
        public void onLiquidChanged()
        {
            setDirty();
        }
    }

    private Tank tank;

	public FluidSharedStorage(SharedStorageHandler storageHandler, int coord, int side) {
		super(storageHandler, coord, side);

		tank = new Tank(1*FluidUtils.B);
	}

	@Override
	public String type() {
		return "liquid";
	}

	@Override
	public NBTTagCompound saveToTag() {
		NBTTagCompound compound = new NBTTagCompound();
        compound.setTag("tank", tank.toTag());

        return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		tank.fromTag(tag.getCompoundTag("tank"));
	}


	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return tank.drain(resource, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	public FluidStack getFluid() {
		return tank.getFluid();
	}


}
