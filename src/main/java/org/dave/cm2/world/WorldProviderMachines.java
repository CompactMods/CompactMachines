package org.dave.cm2.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkGenerator;
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

        return new ChunkGeneratorMachines(this.worldObj);
    }

    @Override
    public boolean canRespawnHere() {
        // TODO: return allowRespawning config option
        // return ConfigurationHandler.allowRespawning;
        return super.canRespawnHere();
    }

    @Override
    public boolean isSurfaceWorld() {
        // TODO: return allowRespawning config option
        // return ConfigurationHandler.allowRespawning;
        return super.canRespawnHere();
    }

    @Override
    protected void generateLightBrightnessTable() {
        for (int i = 0; i < this.lightBrightnessTable.length; i++) {
            this.lightBrightnessTable[i] = 1;
        }
    }

    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0;
    }

    @Override
    public float getCloudHeight() {
        return -5;
    }
}