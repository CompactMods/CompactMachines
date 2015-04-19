package org.dave.CompactMachines.machines.tools;

import java.util.ArrayList;
import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

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

	public static void forceChunkLoad(int coord) {
		World worldObj = CompactMachines.instance.machineHandler.getWorld();
		if (worldObj == null) {
			return;
		}

		// Do not load chunks when the config is set to "never"
		if (ConfigurationHandler.chunkLoadingMode == 0) {
			return;
		}

		Ticket chunkTicket = null;
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
				// Found a ticket that belongs to our mod, this should be true for all cases

				int usedChunks = 0;
				if (data.hasKey("usedChunks")) {
					usedChunks = data.getInteger("usedChunks");
				}

				if (usedChunks < ticket.getMaxChunkListDepth()) {
					chunkTicket = ticket;
					break;
				}
			}
		}

		if (chunkTicket == null) {
			// No existing/free ticket found. Requesting a new one.
			chunkTicket = ForgeChunkManager.requestTicket(CompactMachines.instance, worldObj, Type.NORMAL);
		}

		if (chunkTicket == null) {
			return;
		}

		NBTTagCompound data = chunkTicket.getModData();
		int usedChunks = 0;
		if (data.hasKey("usedChunks")) {
			usedChunks = data.getInteger("usedChunks");
		}

		int[] nbtCoords = new int[chunkTicket.getMaxChunkListDepth()];
		if (data.hasKey("coords")) {
			nbtCoords = data.getIntArray("coords");
			if (nbtCoords.length > chunkTicket.getMaxChunkListDepth()) {
				// TODO: oh oh. we have an old ticket with a bigger chunk-loading limit,
				// we have to request more Tickets! This only happens if you actually change
				// the forge chunk loading limits.
				// --> Support this for the plebs of the internet :(
			}
		} else {
			// initialize with -1
			for (int i = 0; i < nbtCoords.length; i++) {
				nbtCoords[i] = -1;
			}
		}

		// Find "slot" in ticket:
		for (int i = 0; i < nbtCoords.length; i++) {
			if (nbtCoords[i] == -1) {
				nbtCoords[i] = coord;
				break;
			}
		}

		// Each ticket needs to remember for which areas it is responsible
		data.setIntArray("coords", nbtCoords);
		data.setInteger("usedChunks", usedChunks + 1);

		//LogHelper.info("Forcing chunk for room: " + coord);
		ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair((coord * ConfigurationHandler.cubeDistance) >> 4, 0 >> 4));
	}
}
