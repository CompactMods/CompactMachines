package org.dave.cm2.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.world.tools.DimensionTools;

public class WorldProviderMachines extends WorldProvider {
    public WorldProviderMachines() {

    }

    @Override
    public DimensionType getDimensionType() {
        return DimensionTools.baseType;
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorMachines(this.world);
    }

    @Override
    public boolean canRespawnHere() {
        return ConfigurationHandler.MachineSettings.allowRespawning;
    }

    @Override
    public boolean isSurfaceWorld() {
        return ConfigurationHandler.MachineSettings.allowRespawning;
    }

    @Override
    public float getCloudHeight() {
        return -5;
    }
}