package org.dave.cm2.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.dave.cm2.init.Fluidss;

public class MiniaturizationFluidTank extends FluidTank {

    public MiniaturizationFluidTank() {
        super(Fluid.BUCKET_VOLUME * 4);
    }

    @Override
    public boolean canDrain() {
        return false;
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid) {
        return (fluid.getFluid() == Fluidss.miniaturizationFluid);
    }

    public void addTankTagCompound(String tankName, NBTTagCompound compound) {
        NBTTagCompound tankCompound = new NBTTagCompound();
        this.writeToNBT(tankCompound);

        compound.setTag(tankName, tankCompound);
    }
}
