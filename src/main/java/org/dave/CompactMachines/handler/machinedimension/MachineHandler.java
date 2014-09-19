package org.dave.CompactMachines.handler.machinedimension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.WorldUtils;

import com.google.common.collect.ImmutableSetMultimap;

public class MachineHandler extends WorldSavedData {

	int nextCoord;
	private World worldObj;

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
				machine.coords * 64 + 1   , 40 + 1     , 1,
				machine.coords * 64 + size-1, 40 + height-1, size-1
		);

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

					ForgeChunkManager.unforceChunk(ticket, new ChunkCoordIntPair((machine.coords * 64) >> 4, 0 >> 4));

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

	public void teleportPlayerToMachineWorld(EntityPlayerMP player, TileEntityMachine machine) {
		int coord = this.createChunk(machine);
		//LogHelper.info("Teleporting player to: " + coord);
		if (player.dimension != ConfigurationHandler.dimensionId) {
			player.getEntityData().setInteger("oldDimension", player.dimension);
			player.getEntityData().setDouble("oldPosX", player.posX);
			player.getEntityData().setDouble("oldPosY", player.posY);
			player.getEntityData().setDouble("oldPosZ", player.posZ);

			WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, ConfigurationHandler.dimensionId, new TeleporterCM(machineWorld));
		}

		// TODO: OPTIONAL: Think about a way to prevent players building at the teleport location.
		player.setPositionAndUpdate(coord * 64 + 1.5, 42, 1.5);
	}

	public void teleportPlayerOutOfMachineWorld(EntityPlayerMP player) {
		if (player.getEntityData().hasKey("oldPosX"))
		{
			int oldDimension = player.getEntityData().getInteger("oldDimension");
			double oldPosX = player.getEntityData().getDouble("oldPosX");
			double oldPosY = player.getEntityData().getDouble("oldPosY");
			double oldPosZ = player.getEntityData().getDouble("oldPosZ");

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

	public void forceChunkLoad(int coord) {
		if(worldObj == null) {
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

		ForgeChunkManager.forceChunk(chunkTicket, new ChunkCoordIntPair((coord * 64) >> 4, 0 >> 4));
	}

	public int createChunk(TileEntityMachine machine) {
		if(machine.coords != -1) {
			return machine.coords;
		}

		//LogHelper.info("Reserving new coords...");
		machine.coords = nextCoord;
		nextCoord++;

		int size = Reference.getBoxSize(machine.blockMetadata);
		int height = size;

		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);

		machine.interfaces = WorldUtils.generateCube(machineWorld,
				//          x           y           z
				machine.coords * 64,        40,          0,
				machine.coords * 64 + size, 40 + height, size
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
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("nextMachineCoord", nextCoord);
	}

	public void setWorld(World world) {
		this.worldObj = world;
	}

}
