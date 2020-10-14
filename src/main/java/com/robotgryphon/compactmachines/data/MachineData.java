package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MachineData extends WorldSavedData {

    public final static String DATA_NAME = CompactMachines.MODID + "_machines";

    private Map<Integer, CompactMachineData> machineData;

    public MachineData() {
        super(DATA_NAME);
        this.machineData = new HashMap<>();
    }

    @Nonnull
    public static MachineData getMachineData(ServerWorld world) {
        return world.getSavedData().getOrCreate(MachineData::new, DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        if(!nbt.contains("machines"))
            return;

        ListNBT machines = nbt.getList("machines", Constants.NBT.TAG_COMPOUND);
        machines.forEach(machine -> {
            CompactMachineData data = CompactMachineData.fromNBT(machine);
            machineData.put(data.getId(), data);
        });
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        CompoundNBT nbt = new CompoundNBT();
        ListNBT list = new ListNBT();

        machineData.forEach((key, value) -> {
            list.add(value.serializeNBT());
        });

        nbt.put("machines", list);
        return compound;
    }

    public static int getNextMachineId(ServerWorld world) {
        MachineData machineData = getMachineData(world);
        if(machineData.machineData == null)
            return 0;

        return machineData.machineData.size() + 1;
    }

    public boolean addToMachineData(int newID, CompactMachineData compactMachineData) {
        if(machineData.containsKey(newID))
            return false;

        this.machineData.put(newID, compactMachineData);
        this.markDirty();
        return true;
    }
}
