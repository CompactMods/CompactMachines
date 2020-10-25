package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;

public class MachineData extends WorldSavedData {

    public final static String DATA_NAME = CompactMachines.MODID + "_machines";

    public MachineData() {
        super(DATA_NAME);
    }

    @Nonnull
    public static MachineData getMachineData(ServerWorld world) {
        DimensionSavedDataManager sd = world.getSavedData();
        return sd.getOrCreate(MachineData::new, DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        CompactMachineMemoryData.INSTANCE.deserializeNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        return CompactMachineMemoryData.INSTANCE.serializeNBT();
    }
}
