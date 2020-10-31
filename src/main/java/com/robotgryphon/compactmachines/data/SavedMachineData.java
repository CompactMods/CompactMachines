package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;

/**
 * Basically a wrapper around CompactMachineServerData. Allows read/write to disk.
 */
public class SavedMachineData extends WorldSavedData {

    public final static String DATA_NAME = CompactMachines.MODID + "_machines";
    private static CompactMachineServerData SERVER_DATA;

    public SavedMachineData() {
        super(DATA_NAME);
    }

    @Nonnull
    public static SavedMachineData getMachineData(MinecraftServer server) {
        ServerWorld compactWorld = server.getWorld(Registrations.COMPACT_DIMENSION);
        if (compactWorld == null)
        {
            CompactMachines.LOGGER.error("No compact dimension found. Falling back to overworld.");
            return server
                    .getWorld(World.OVERWORLD)
                    .getSavedData()
                    .getOrCreate(SavedMachineData::new, DATA_NAME);
        }

        DimensionSavedDataManager sd = compactWorld.getSavedData();
        return sd.getOrCreate(SavedMachineData::new, DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
        SERVER_DATA = CompactMachineServerData.fromNbt(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        return SERVER_DATA.serializeNBT(compound);
    }

    public CompactMachineServerData getServerData() {
        return SERVER_DATA;
    }
}
