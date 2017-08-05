package org.dave.compactmachines3.item.psd;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nullable;

public class PSDCapabilityProvider implements ICapabilityProvider {
    private ItemStack stack;
    private PSDFluidStorage storage;

    public PSDCapabilityProvider(ItemStack stack) {
        this.stack = stack;
        this.storage = new PSDFluidStorage(stack);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new PSDFluidStorage(stack));
        }

        return null;
    }
}
