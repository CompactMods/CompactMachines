package org.dave.cm2.item.psd;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.dave.cm2.init.Fluidss;

public class PSDFluidStorage extends FluidTank {
    private ItemStack stack;

    public PSDFluidStorage(ItemStack stack) {
        super(Fluid.BUCKET_VOLUME * 4);

        this.stack = stack;
        if(stack.hasTagCompound() && stack.getTagCompound().hasKey("fluid") && stack.getTagCompound().getInteger("fluid") > 0) {
            this.fill(new FluidStack(Fluidss.miniaturizationFluid, stack.getTagCompound().getInteger("fluid")), true);
        }
    }

    @Override
    public boolean canDrain() {
        return false;
    }

    @Override
    public boolean canFill() {
        return true;
    }

    private void saveNbtToStack() {
        if(!this.stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger("fluid", getFluidAmount());
    }

    @Override
    public int fillInternal(FluidStack resource, boolean doFill) {
        int filled = super.fillInternal(resource, doFill);
        if(filled > 0 && doFill) {
            saveNbtToStack();
        }

        return filled;
    }

    @Override
    public FluidStack drainInternal(int maxDrain, boolean doDrain) {
        FluidStack drained = super.drainInternal(maxDrain, doDrain);
        if(drained != null && drained.amount > 0 && doDrain) {
            saveNbtToStack();
        }

        return drained;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return (fluid.getFluid() == Fluidss.miniaturizationFluid);
    }
}
