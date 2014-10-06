package org.dave.CompactMachines.handler.machinedimension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.WorldUtils;

import com.google.common.collect.ImmutableSetMultimap;

public class MachineHandler extends WorldSavedData {

	int nextCoord;
	private World worldObj;

	HashMap<Integer, double[]> spawnPoints = new HashMap<Integer, double[]>();

	public MachineHandler(String s) {
		super(s);

		nextCoord = 0;
	}

	public MachineHandler(World worldObj)	{
		this("MachineHandler");
		this.worldObj = worldObj;
	}

	public void harvestMachine(TileEntityMachine machine) {
		if(machine.coords == -1) {
			return;
		}

		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);

		int size = Reference.getBoxSize(machine.blockMetadata);
		int height = size;

		List<ItemStack> stacks = WorldUtils.harvestCube(machineWorld,
				//   x           y           z
				machine.coords * ConfigurationHandler.cubeDistance + 1   , 40 + 1     , 1,
				machine.coords * ConfigurationHandler.cubeDistance + size-1, 40 + height-1, size-1
		);

		for(ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			ItemSharedStorage storage = machine.getStorage(dir.ordinal());
			ItemStack storedStack = storage.getStackInSlot(0);
			if(storedStack != null && storedStack.stackSize > 0) {
				stacks.add(storedStack);
			}
			storage.setInventorySlotContents(0, null);
			storage.setDirty();
		}

		int droppedStacks = 0;
		for(ItemStack stack : stacks) {
			if(ConfigurationHandler.maxDroppedStacks != -1 && droppedStacks >= ConfigurationHandler.maxDroppedStacks) {
				return;
			}

			EntityItem entityitem = new EntityItem(machine.getWorldObj(), machine.xCoord, machine.yCoord + 0.5F, machine.zCoord, stack);

			entityitem.lifespan = 1200;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.05F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3;
			machine.getWorldObj().spawnEntityInWorld(entityitem);
			droppedStacks++;
		}

		return;
	}

	public boolean isCoordChunkLoaded(TileEntityMachine machine) {
		return isCoordChunkLoaded(machine.coords);
	}

	public boolean isCoordChunkLoaded(int coords) {
		if(coords == -1) {
			return false;
		}

		// Find the ticket that is being used for this machines chunk
		ImmutableSetMultimap<ChunkCoordIntPair, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(worldObj);

		Iterator ticketIterator = existingTickets.values().iterator();
		ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
		while(ticketIterator.hasNext()) {
			Ticket ticket = (Ticket)ticketIterator.next();
			if(visitedTickets.contains(ticket.hashCode())) {
				continue;
			}
			visitedTickets.add(ticket.hashCode());

			NBTTagCompound data = ticket.getModData();
			if(data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				for (int i = 0; i < nbtCoords.length; i++) {
					if(nbtCoords[i] == coords) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public void disableMachine(TileEntityMachine machine) {
		if(machine.coords == -1) {
			return;
		}

		// Find the ticket that is being used for this machines chunk
		ImmutableSetMultimap<ChunkCoordIntPair, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(worldObj);

		Iterator ticketIterator = existingTickets.values().iterator();
		ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
		while(ticketIterator.hasNext()) {
			Ticket ticket = (Ticket)ticketIterator.next();
			if(visitedTickets.contains(ticket.hashCode())) {
				continue;
			}

			visitedTickets.add(ticket.hashCode());

			NBTTagCompound data = ticket.getModData();
			if(data.hasKey("coords")) {
				int[] nbtCoords = data.getIntArray("coords");

				boolean foundMatch = false;
				for (int i = 0; i < nbtCoords.length; i++) {
					if(nbtCoords[i] != machine.coords) {
						continue;
					}

					//LogHelper.info("Unforcing chunk for room: " + machine.coords);
					ForgeChunkManager.unforceChunk(ticket, new ChunkCoordIntPair((machine.coords * ConfigurationHandler.cubeDistance) >> 4, 0 >> 4));

					int usedChunks = 0;
					if(data.hasKey("usedChunks")) {
						usedChunks = data.getInteger("usedChunks");
					}

					if(usedChunks < 2) {
						ForgeChunkManager.releaseTicket(ticket);
					} else {
						nbtCoords[i] = -1;
						data.setInteger("usedChunks", usedChunks - 1);
						data.setIntArray("coords", nbtCoords);
					}

					foundMatch = true;
					break;
				}

				if(foundMatch) {
					break;
				}
			}
		}

		this.markDirty();
	}

	public void setCoordSpawnpoint(EntityPlayerMP player) {
		NBTTagCompound playerNBT = player.getEntityData();
		if(!playerNBT.hasKey("coordHistory")) {
			return;
		}

		NBTTagList coordHistory = playerNBT.getTagList("coordHistory", 10);
		if(coordHistory.tagCount() == 0) {
			return;
		}

		int lastCoord = coordHistory.getCompoundTagAt(coordHistory.tagCount()-1).getInteger("coord");
		if(lastCoord > -1) {
			//LogHelper.info("Saved spawnpoint for " + lastCoord + ": {" + player.posX +", "+ player.posY +", "+ player.posZ + "}");
			spawnPoints.put(lastCoord, new double[]{player.posX, player.posY, player.posZ});
		}

		this.markDirty();
	}

	public void teleportPlayerToCoords(EntityPlayerMP player, int coord, boolean isReturning) {
		//LogHelper.info("Teleporting player to: " + coord);
		NBTTagCompound playerNBT = player.getEntityData();

		if (player.dimension != ConfigurationHandler.dimensionId) {
			playerNBT.setInteger("oldDimension", player.dimension);
			playerNBT.setDouble("oldPosX", player.posX);
			playerNBT.setDouble("oldPosY", player.posY);
			playerNBT.setDouble("oldPosZ", player.posZ);

			WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, ConfigurationHandler.dimensionId, new TeleporterCM(machineWorld));

			// Since the player is currently not in the machine dimension, we want to clear
			// his coord history - in case he exited the machine world not via a shrinking device
			// which automatically clears the last entry in the coord history.
			if(playerNBT.hasKey("coordHistory")) {
				playerNBT.removeTag("coordHistory");
			}
		}

		if(!isReturning) {
			NBTTagList coordHistory;
			if(playerNBT.hasKey("coordHistory")) {
				coordHistory = playerNBT.getTagList("coordHistory", 10);
			} else {
				coordHistory = new NBTTagList();
			}
			NBTTagCompound toAppend = new NBTTagCompound();
			toAppend.setInteger("coord", coord);

			coordHistory.appendTag(toAppend);
			playerNBT.setTag("coordHistory", coordHistory);
		}

		// TODO: Set default spawn point to a better location
		double[] destination = new double[]{coord * ConfigurationHandler.cubeDistance + 1.5, 42, 1.5};
		if(spawnPoints.containsKey(coord)) {
			destination = spawnPoints.get(coord);
		}

		player.setPositionAndUpdate(destination[0],destination[1],destination[2]);
	}

	public void teleportPlayerToMachineWorld(EntityPlayerMP player, TileEntityMachine machine) {
		int coords = this.createChunk(machine);
		teleportPlayerToCoords(player, coords, false);
	}


	public void teleportPlayerOutOfMachineDimension(EntityPlayerMP player) {
		NBTTagCompound playerNBT = player.getEntityData();
		if (playerNBT.hasKey("oldPosX"))
		{
			int oldDimension = playerNBT.getInteger("oldDimension");
			double oldPosX = playerNBT.getDouble("oldPosX");
			double oldPosY = playerNBT.getDouble("oldPosY");
			double oldPosZ = playerNBT.getDouble("oldPosZ");

			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, oldDimension, new TeleporterCM(MinecraftServer.getServer().worldServerForDimension(oldDimension)));
			player.setPositionAndUpdate(oldPosX, oldPosY, oldPosZ);
		}
		else
		{
			ChunkCoordinates cc = MinecraftServer.getServer().worldServerForDimension(0).provider.getRandomizedSpawnPoint();
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, 0, new TeleporterCM(MinecraftServer.getServer().worldServerForDimension(0)));

			player.setPositionAndUpdate(cc.posX, cc.posY, cc.posZ);
		}
	}

	public void teleportPlayerBack(EntityPlayerMP player) {
		NBTTagCompound playerNBT = player.getEntityData();
		if (playerNBT.hasKey("coordHistory")) {
			NBTTagList coordHistory = playerNBT.getTagList("coordHistory", 10);
			if(coordHistory.tagCount() == 0) {
				// No coord history so far, teleport back to overworld
				teleportPlayerOutOfMachineDimension(player);
			} else {
				// Remove the last tag, then teleport to the new last
				coordHistory.removeTag(coordHistory.tagCount()-1);
				if(coordHistory.tagCount() == 0) {
					teleportPlayerOutOfMachineDimension(player);
					return;
				}

				int coord = coordHistory.getCompoundTagAt(coordHistory.tagCount()-1).getInteger("coord");
				teleportPlayerToCoords(player, coord, true);
			}
		}
	}



	public void forceChunkLoad(int coord) {
		if(worldObj == null) {
			return;
		}

		// Do not load chunks when the config is set to "never"
		if(ConfigurationHandler.chunkLoadingMode == 0) {
			return;
		}

		Ticket chunkTicket = null;
		ImmutableSetMultimap<ChunkCoordIntPair, Ticket> existingTickets = ForgeChunkManager.getPersistentChunksFor(worldObj);

		Iterator ticketIterator = existingTickets.values().iterator();
		ArrayList<Integer> visitedTickets = new ArrayList<Integer>();
		while(ticketIterator.hasNext()) {
			Ticket ticket = (Ticket)ticketIterator.next();
			if(visitedTickets.contains(ticket.hashCode())) {
				continue;
			}

			visitedTickets.add(ticket.hashCode());

			NBTTagCompound data = ticket.getModData();
			if(data.hasKey("coords")) {
				// Found a ticket that belongs to our mod, this should be true for all cases

				int usedChunks = 0;
				if(data.hasKey("usedChunks")) {
					usedChunks = data.getInteger("usedChunks");
				}

				if(usedChunks < ticket.getMaxChunkListDepth()) {
					chunkTicket = ticket;
					break;
				}
			}
		}

		if(chunkTicket == null) {
			// No existing/free ticket found. Requesting a new one.
			chunkTicket = ForgeChunkManager.requestTicket(CompactMachines.instance, worldObj, Type.NORMAL);
		}

		if(chunkTicket == null) {
			return;
		}

		NBTTagCompound data = chunkTicket.getModData();
		int usedChunks = 0;
		if(data.hasKey("usedChunks")) {
			usedChunks = data.getInteger("usedChunks");
		}

		int[] nbtCoords = new int[chunkTicket.getMaxChunkListDepth()];
		if(data.hasKey("coords")) {
			nbtCoords = data.getIntArray("coords");
			if(nbtCoords.length > chunkTicket.getMaxChunkListDepth()) {
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
			if(nbtCoords[i] == -1) {
				nbtCoords[i] = coord;
				break;
			}
		}

		// Each ticket needs to remember for which areas it is responsible
		data.setIntArray("coords", nbtCoords);
		data.setInteger("usedChunks", usedChunks+1);

		//LogHelper.info("Forcing chunk for room: " + coord);
		ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair((coord * ConfigurationHandler.cubeDistance) >> 4, 0 >> 4));
	}

	public int createChunk(TileEntityMachine machine) {
		if(machine.coords != -1) {
			return machine.coords;
		}

		//LogHelper.info("Reserving new coords...");
		machine.coords = nextCoord;
		nextCoord++;

		int size = Reference.getBoxSize(machine.meta);
		int height = size;

		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);

		machine.interfaces = WorldUtils.generateCube(machineWorld,
				//          x           y           z
				machine.coords * ConfigurationHandler.cubeDistance,        40,          0,
				machine.coords * ConfigurationHandler.cubeDistance + size, 40 + height, size
		);

		// After creating the Block, make sure the TileEntities inside have their information ready.
		for(int i = 0; i < 6; i++) {
			Vec3 pos = machine.interfaces.get(i);
			TileEntityInterface te = (TileEntityInterface)machineWorld.getTileEntity((int)pos.xCoord, (int)pos.yCoord, (int)pos.zCoord);
			te.setCoordSide(machine.coords, i);
		}

		machine.markDirty();

		this.forceChunkLoad(machine.coords);
		this.markDirty();

		return machine.coords;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		nextCoord = nbt.getInteger("nextMachineCoord");

		if(nbt.hasKey("spawnpoints")) {
			spawnPoints.clear();
			NBTTagList tagList = nbt.getTagList("spawnpoints", 10);
			for (int i = 0; i < tagList.tagCount(); i++) {
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				int coords = tag.getInteger("coords");
				double[] positions = new double[]{tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")};

				spawnPoints.put(coords, positions);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("nextMachineCoord", nextCoord);

		NBTTagList tagList = new NBTTagList();
		Iterator sp = spawnPoints.keySet().iterator();
		while(sp.hasNext()) {
			int coords = (Integer)sp.next();
			double[] positions = spawnPoints.get(coords);

			NBTTagCompound tag = new NBTTagCompound();
			tag.setInteger("coords", coords);
			tag.setDouble("x", positions[0]);
			tag.setDouble("y", positions[1]);
			tag.setDouble("z", positions[2]);
			tagList.appendTag(tag);
		}

		nbt.setTag("spawnpoints", tagList);
	}

	public void setWorld(World world) {
		this.worldObj = world;
	}

}
