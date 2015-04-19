package org.dave.CompactMachines.machines.world;

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
		if (ConfigurationHandler.chunkLoadingMode == 0) {
			LogHelper.info("Chunkloading is disabled. Skipping...");
			return;
		}

		if (ConfigurationHandler.chunkLoadingMode == 2) {
			LogHelper.info("Chunkloading is in smart mode. Releasing previously requested tickets...");
			for (Ticket ticket : tickets) {
				ForgeChunkManager.releaseTicket(ticket);
			}
			return;
		}

		LogHelper.info("Chunkloading is in always mode. Loading all previously loaded chunks.");
		for (Ticket ticket : tickets) {
			NBTTagCompound data = ticket.getModData();
			if (data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				boolean foundMatch = false;
				for (int i = 0; i < nbtCoords.length; i++) {
					if (nbtCoords[i] == -1) {
						continue;
					}

					//LogHelper.info("Forcing chunk for room: " + nbtCoords[i]);
					ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair((nbtCoords[i] * ConfigurationHandler.cubeDistance) >> 4, 0 >> 4));
				}

				// Ticket has no valid coords stored, releasing it.
				if (!foundMatch) {
					ForgeChunkManager.releaseTicket(ticket);
				}
			}
		}
	}
}
