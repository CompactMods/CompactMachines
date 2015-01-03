package org.dave.CompactMachines.utility;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class WorldCoords {
	private World world;
	public int dimensionId;
	public int x,y,z;

	public WorldCoords(World world, int x, int y, int z) {
		this.world = world;
		this.dimensionId = world.provider.dimensionId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public World getWorld() {
		if(world != null) {
			return world;
		} else {
			return MinecraftServer.getServer().worldServerForDimension(dimensionId);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof WorldCoords)) {
			return false;
		}

		WorldCoords other = (WorldCoords)obj;
		if(other.x != x || other.y != y || other.z != z || other.dimensionId != dimensionId) {
			return false;
		}

		return true;
	}
}
