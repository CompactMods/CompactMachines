package org.dave.CompactMachines.machines.world;

import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;

import org.dave.CompactMachines.handler.ConfigurationHandler;

public class WorldProviderMachines extends WorldProvider {

	public WorldProviderMachines() {}

	@Override
	protected void generateLightBrightnessTable() {
		for (int i = 0; i < this.lightBrightnessTable.length; i++)
		{
			this.lightBrightnessTable[i] = 1;
		}
	}

	@Override
	public boolean isDaytime() {
		if(ConfigurationHandler.allowRespawning) {
			return false;
		}

		return super.isDaytime();
	}

	@Override
	public boolean isSurfaceWorld()	{
		return ConfigurationHandler.allowRespawning;
	}

	@Override
	public boolean canRespawnHere()	{
		return ConfigurationHandler.allowRespawning;
	}

	@Override
	public boolean doesXZShowFog(int par1, int par2) {
		return true;
	}

	@Override
	public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
		super.setAllowedSpawnTypes(false, false);
	}

	@Override
	public String getDimensionName() {
		return "CompactMachinesWorld";
	}

	@Override
	public void registerWorldChunkManager() {
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.sky, 0F);
		this.dimensionId = ConfigurationHandler.dimensionId;
	}

	@Override
	public IChunkProvider createChunkGenerator() {
		return new ChunkProviderMachines(this.worldObj);
	}

	@Override
	public float[] calcSunriseSunsetColors(float par1, float par2) {
		return new float[] { 0, 0, 0, 0 };
	}

	@Override
	public float calculateCelestialAngle(long par1, float par3)	{
		return 0;
	}

	@Override
	public float getCloudHeight() {
		return -5;
	}

	@Override
	public String getWelcomeMessage() {
		return StatCollector.translateToLocal("loading.cm:enter");
	}

	@Override
	public String getDepartMessage() {
		return StatCollector.translateToLocal("loading.cm:leave");
	}

}
