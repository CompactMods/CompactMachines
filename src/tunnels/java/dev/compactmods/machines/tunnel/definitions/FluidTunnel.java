package dev.compactmods.machines.tunnel.definitions;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;

public class FluidTunnel implements TunnelDefinition, CapabilityTunnel<FluidTunnel.Instance> {
    @Override
    public int ringColor() {
        return FastColor.ARGB32.color(255, 0, 138, 224);
    }

    @Override
    public ImmutableSet<Capability<?>> getSupportedCapabilities() {
        return ImmutableSet.of(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }

    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> type, FluidTunnel.Instance instance) {
        if(type == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return instance.lazy().cast();

        return LazyOptional.empty();
    }

    @Override
    public FluidTunnel.Instance newInstance(BlockPos position, Direction side) {
        return new Instance(4000);
    }

    public class Instance implements TunnelInstance, INBTSerializable<CompoundTag> {

        private final FluidTank handler;
        private final LazyOptional<IFluidHandler> lazy;

        public Instance(int size) {
            this.handler = new FluidTank(size);
            this.lazy = LazyOptional.of(this::getHandler);
        }

        @Nonnull
        private IFluidHandler getHandler() {
            return this.handler;
        }

        public LazyOptional<IFluidHandler> lazy() {
            return this.lazy;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag data = new CompoundTag();
            return handler.writeToNBT(data);
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            handler.readFromNBT(nbt);
        }
    }
}
