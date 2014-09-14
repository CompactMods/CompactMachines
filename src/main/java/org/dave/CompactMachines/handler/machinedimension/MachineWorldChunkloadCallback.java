package org.dave.CompactMachines.handler.machinedimension;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class MachineWorldChunkloadCallback implements LoadingCallback {

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {

		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			if(data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				boolean foundMatch = false;
				for (int i = 0; i < nbtCoords.length; i++) {
					if(nbtCoords[i] == -1) {
						continue;
					}

					ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair((nbtCoords[i] * 64) >> 4, 0 >> 4));
				}
			}

			/*
			if(!CompactMachines.instance.machineHandler.coordTickets.containsKey(coord)) {
				CompactMachines.instance.machineHandler.coordTickets.put(coord, ticket);
			}
			*/

		}
	}
}
