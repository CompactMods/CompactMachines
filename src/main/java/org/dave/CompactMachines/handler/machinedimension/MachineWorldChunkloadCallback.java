package org.dave.CompactMachines.handler.machinedimension;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.utility.LogHelper;

public class MachineWorldChunkloadCallback implements LoadingCallback {

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		// Do not load chunks when the config is set to "never"
		if(ConfigurationHandler.chunkLoadingMode == 0) {
			LogHelper.info("Chunkloading is disabled. Skipping...");
			return;
		}

		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			if(data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				boolean foundMatch = false;
				for (int i = 0; i < nbtCoords.length; i++) {
					if(nbtCoords[i] == -1) {
						continue;
					}

					LogHelper.info("Forcing chunk for room: " + nbtCoords[i]);
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
