package org.dave.CompactMachines.handler.machinedimension;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

import com.google.common.collect.ImmutableSetMultimap;

public class ChunkLoadingTools {

	public static void disableMachine(TileEntityMachine machine) {
		disableMachine(machine.coords);
	}

	public static void disableMachine(int coords) {
		if (coords == -1) {
			return;
		}

		// Find the ticket that is being used for this machines chunk
		World worldObj = CompactMachines.instance.machineHandler.getWorld();
		ImmutableSetMultimap<ChunkCoordIntPair, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(worldObj);

		Iterator ticketIterator = existingTickets.values().iterator();
		ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
		while (ticketIterator.hasNext()) {
			Ticket ticket = (Ticket) ticketIterator.next();
			if (visitedTickets.contains(ticket.hashCode())) {
				continue;
			}

			visitedTickets.add(ticket.hashCode());

			NBTTagCompound data = ticket.getModData();
			if (data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				boolean foundMatch = false;
				for (int i = 0; i < nbtCoords.length; i++) {
					if (nbtCoords[i] != coords) {
						continue;
					}

					//LogHelper.info("Unforcing chunk for room: " + machine.coords);
					ForgeChunkManager.unforceChunk(ticket, new ChunkCoordIntPair((coords * ConfigurationHandler.cubeDistance) >> 4, 0 >> 4));

					int usedChunks = 0;
					if (data.hasKey("usedChunks")) {
						usedChunks = data.getInteger("usedChunks");
					}

					if (usedChunks < 2) {
						ForgeChunkManager.releaseTicket(ticket);
					} else {
						nbtCoords[i] = -1;
						data.setInteger("usedChunks", usedChunks - 1);
						data.setIntArray("coords", nbtCoords);
					}

					foundMatch = true;
					break;
				}

				if (foundMatch) {
					break;
				}
			}
		}

		CompactMachines.instance.machineHandler.markDirty();
	}

	public static boolean isCoordChunkLoaded(TileEntityMachine machine) {
		return isCoordChunkLoaded(machine.coords);
	}

	public static boolean isCoordChunkLoaded(int coords) {
		if (coords == -1) {
			return false;
		}

		// Find the ticket that is being used for this machines chunk
		World worldObj = CompactMachines.instance.machineHandler.getWorld();
		ImmutableSetMultimap<ChunkCoordIntPair, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(worldObj);

		Iterator ticketIterator = existingTickets.values().iterator();
		ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
		while (ticketIterator.hasNext()) {
			Ticket ticket = (Ticket) ticketIterator.next();
			if (visitedTickets.contains(ticket.hashCode())) {
				continue;
			}
			visitedTickets.add(ticket.hashCode());

			NBTTagCompound data = ticket.getModData();
			if (data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				for (int i = 0; i < nbtCoords.length; i++) {
					if (nbtCoords[i] == coords) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
