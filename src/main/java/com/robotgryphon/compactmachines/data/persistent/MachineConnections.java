package com.robotgryphon.compactmachines.data.persistent;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.graph.CompactMachineConnectionGraph;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

public class MachineConnections extends WorldSavedData {
    public static final String DATA_NAME = CompactMachines.MOD_ID + "_connections";

    private CompactMachineConnectionGraph graph;

    public MachineConnections() {
        super(DATA_NAME);
        graph = new CompactMachineConnectionGraph();
    }

    public static MachineConnections instance(MinecraftServer server) {
        ServerWorld compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionSavedDataManager sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(MachineConnections::new, DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {

    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        return nbt;
    }
}
