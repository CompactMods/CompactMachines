package org.dave.CompactMachines.handler.machinedimension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.world.World;

import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.LogHelper;
import org.dave.CompactMachines.utility.WorldCoords;

public class EntangleRegistry {
	// [ roomId -> [ instance -> position ] ];
	private HashMap<Integer, HashMap<Integer, WorldCoords>> reg;

	public EntangleRegistry() {
		reg = new HashMap<Integer, HashMap<Integer, WorldCoords>>();
		LogHelper.info("Creating new Entangle Registry");
	}

	public void clear() {
		LogHelper.info("Clearing Entangle Registry");
		reg.clear();
	}

	public HashMap<Integer, WorldCoords> getMachinesForCoord(int coord) {
		return reg.get(coord);
	}

	public boolean hasRemainingMachines(int coord) {
		if(!reg.containsKey(coord)) {
			return false;
		}

		HashMap<Integer, WorldCoords> list = reg.get(coord);
		return (list.size() > 0);
	}

	public void removeMachineTile(TileEntityMachine tileEntityMachine) {
		removeMachineTile(tileEntityMachine.coords, tileEntityMachine.getWorldObj(), tileEntityMachine.xCoord, tileEntityMachine.yCoord, tileEntityMachine.zCoord);
	}

	public void removeMachineTile(int coord, World world, int x, int y, int z) {
		if(!reg.containsKey(coord)) {
			return;
		}

		HashMap<Integer, WorldCoords> list = reg.get(coord);

		WorldCoords pos = new WorldCoords(world, x, y, z);

		// Find existing
		int iHighest = 0;
		Iterator iterator = list.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry pair = (Map.Entry)iterator.next();
			if(pos.equals(pair.getValue())) {
				list.remove(pair.getKey());
				return;
			}
		}
	}

	public int registerMachineTile(int coord, World world, int x, int y, int z) {
		HashMap<Integer, WorldCoords> list;
		if(!reg.containsKey(coord)) {
			list = new HashMap<Integer, WorldCoords>();
		} else {
			list = reg.get(coord);
		}

		WorldCoords pos = new WorldCoords(world, x, y, z);

		// Find existing
		int iHighest = -1;
		Iterator iterator = list.entrySet().iterator();
		while(iterator.hasNext()) {
			Map.Entry pair = (Map.Entry)iterator.next();
			if(pos.equals(pair.getValue())) {
				return (Integer) pair.getKey();
			}

			int pairKey = (Integer)pair.getKey();
			if(pairKey > iHighest) {
				iHighest = (Integer)pair.getKey();
			}
		}

		// Add it
		list.put(iHighest + 1, pos);
		reg.put(coord, list);

		return iHighest+1;
	}

	public int registerMachineTile(TileEntityMachine tileEntityMachine) {
		return registerMachineTile(tileEntityMachine.coords, tileEntityMachine.getWorldObj(), tileEntityMachine.xCoord, tileEntityMachine.yCoord, tileEntityMachine.zCoord);
	}

}
