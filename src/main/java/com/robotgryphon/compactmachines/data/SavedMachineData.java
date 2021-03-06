package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
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

    public final static String DATA_NAME = CompactMachines.MOD_ID + "_machines";
    private static CompactMachineServerData DATA;

    public SavedMachineData() {
        super(DATA_NAME);
    }

    @Nonnull
    public static SavedMachineData getInstance(MinecraftServer server) {
        ServerWorld compactWorld = server.getWorld(Registration.COMPACT_DIMENSION);
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
        DATA = CompactMachineServerData.fromNbt(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if(DATA != null)
            return DATA.serializeNBT(compound);

        return compound;
    }

    public CompactMachineServerData getData() {
        if(DATA == null)
            DATA = new CompactMachineServerData();

        return DATA;
    }
}
