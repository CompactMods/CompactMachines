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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class ForgeEnergyTunnel extends ForgeRegistryEntry<TunnelDefinition>
        implements TunnelDefinition, CapabilityTunnel<ForgeEnergyTunnel.Instance> {
    @Override
    public int ringColor() {
        return FastColor.ARGB32.color(255, 0, 166, 88);
    }

    @Override
    public ImmutableSet<Capability<?>> getSupportedCapabilities() {
        return ImmutableSet.of(CapabilityEnergy.ENERGY);
    }

    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> type, Instance instance) {
        if (type == CapabilityEnergy.ENERGY) {
            return instance.lazy().cast();
        }

        return LazyOptional.empty();
    }

    @Override
    public Instance newInstance(BlockPos position, Direction side) {
        return new Instance();
    }

    public static class Instance implements TunnelInstance, INBTSerializable<CompoundTag> {

        private final int DEFAULT_STORAGE = 10000;
        private EnergyStorage storage;
        private final LazyOptional<IEnergyStorage> lazy;

        public Instance() {
            this.storage = new EnergyStorage(DEFAULT_STORAGE);
            this.lazy = LazyOptional.of(this::getStorage);
        }

        @Nonnull
        public EnergyStorage getStorage() {
            return this.storage;
        }

        public LazyOptional<IEnergyStorage> lazy() {
            return lazy;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag data = new CompoundTag();
            data.put("storage", storage.serializeNBT());
            return data;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if(!nbt.contains("storage")) {
                this.storage = new EnergyStorage(DEFAULT_STORAGE);
                return;
            }

            final var stor = nbt.get("storage");
            storage.deserializeNBT(stor);
        }
    }
}
