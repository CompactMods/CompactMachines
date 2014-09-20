package org.dave.CompactMachines.integration.fluid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import org.dave.CompactMachines.handler.SharedStorageHandler;
import org.dave.CompactMachines.integration.AbstractSharedStorage;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.FluidUtils;

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
		NBTTagCompound compound = prepareTagCompound();
		compound.setTag("tank", tank.toTag());
		return compound;
	}

	@Override
	public void loadFromTag(NBTTagCompound tag) {
		loadHoppingModeFromCompound(tag);
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

	private void hopToTileEntity(TileEntity tileEntityOutside) {
		FluidStack stack = getFluid().copy();
		if(stack == null || stack.amount == 0) {
			return;
		}

		if(cooldown == max_cooldown) {
			cooldown = 0;
		} else {
			cooldown++;
			return;
		}

		if(tileEntityOutside instanceof IFluidHandler) {
			IFluidHandler fh = (IFluidHandler)tileEntityOutside;

			if(fh.canFill(ForgeDirection.getOrientation(side).getOpposite(), stack.getFluid())) {
				int filled = fh.fill(ForgeDirection.getOrientation(side).getOpposite(), stack, false);
				if(filled > 0) {
					//LogHelper.info("Simulation filled: " + filled);
					fh.fill(ForgeDirection.getOrientation(side).getOpposite(), stack, true);
					this.drain(ForgeDirection.UNKNOWN, filled, true);
					tileEntityOutside.markDirty();
				}
			}
		}
	}

	@Override
	public void hopToOutside(TileEntityMachine tileEntityMachine, TileEntity tileEntityOutside) {
		hopToTileEntity(tileEntityOutside);
	}

	@Override
	public void hopToInside(TileEntityInterface tileEntityInterface, TileEntity tileEntityInside) {
		hopToTileEntity(tileEntityInside);
	}

}
