package org.dave.CompactMachines.machines.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.machines.MachineSaveData;
import org.dave.CompactMachines.machines.world.TeleporterCM;
import org.dave.CompactMachines.network.MessagePlayerRotation;
import org.dave.CompactMachines.network.PacketHandler;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.PlayerUtils;

public class TeleportTools {

	public static void teleportPlayerToCoords(EntityPlayerMP player, int coord, boolean isReturning) {
		//LogHelper.info("Teleporting player to: " + coord);
		NBTTagCompound playerNBT = player.getEntityData();

		// Grab the CompactMachines entry from the player NBT data
		NBTTagCompound cmNBT;
		if (playerNBT.hasKey(Reference.MOD_ID)) {
			cmNBT = playerNBT.getCompoundTag(Reference.MOD_ID);
		} else {
			cmNBT = new NBTTagCompound();
			playerNBT.setTag(Reference.MOD_ID, cmNBT);
		}

		if (player.dimension != ConfigurationHandler.dimensionId) {
			cmNBT.setInteger("oldDimension", player.dimension);
			cmNBT.setDouble("oldPosX", player.posX);
			cmNBT.setDouble("oldPosY", player.posY);
			cmNBT.setDouble("oldPosZ", player.posZ);

			int oldDimension = player.dimension;

			WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
			MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, ConfigurationHandler.dimensionId, new TeleporterCM(machineWorld));

			// If this is not being called teleporting from The End ends up without
			// the client knowing about any blocks, i.e. blank screen, no blocks, but
			// server collisions etc.
			if(oldDimension == 1) {
				machineWorld.spawnEntityInWorld(player);
			}

			// Since the player is currently not in the machine dimension, we want to clear
			// his coord history - in case he exited the machine world not via a shrinking device
			// which automatically clears the last entry in the coord history.
			if (playerNBT.hasKey("coordHistory")) {
				playerNBT.removeTag("coordHistory");
			}
		}

		if (!isReturning) {
			NBTTagList coordHistory;
			if (playerNBT.hasKey("coordHistory")) {
				coordHistory = playerNBT.getTagList("coordHistory", 10);
			} else {
				coordHistory = new NBTTagList();
			}
			NBTTagCompound toAppend = new NBTTagCompound();
			toAppend.setInteger("coord", coord);

			coordHistory.appendTag(toAppend);
			playerNBT.setTag("coordHistory", coordHistory);
		}

		MachineSaveData mHandler = CompactMachines.instance.machineHandler;
		double[] destination = mHandler.getSpawnLocation(coord);

		// Check whether the spawn location is blocked
		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
		int dstX = (int) Math.floor(destination[0]);
		int dstY = (int) Math.floor(destination[1]);
		int dstZ = (int) Math.floor(destination[2]);

		if (!machineWorld.isAirBlock(dstX, dstY, dstZ) || !machineWorld.isAirBlock(dstX, dstY + 1, dstZ)) {
			// If it is blocked, try to find a better position
			double[] bestSpot = findBestSpawnLocation(machineWorld, coord);
			if (bestSpot != null) {
				destination = bestSpot;
			}

			// otherwise teleport to the default location... player will probably die though.
		}

		player.setPositionAndUpdate(destination[0], destination[1], destination[2]);

		if(destination.length == 5) {
			MessagePlayerRotation packet = new MessagePlayerRotation((float) destination[3], (float) destination[4]);
			PacketHandler.INSTANCE.sendTo(packet, player);
		}
	}

	public static void teleportPlayerToMachineWorld(EntityPlayerMP player, TileEntityMachine machine) {
		int coords = CompactMachines.instance.machineHandler.createOrGetChunk(machine);
		teleportPlayerToCoords(player, coords, false);
	}

	public static void teleportPlayerOutOfMachineDimension(EntityPlayerMP player) {
		NBTTagCompound playerNBT = player.getEntityData();

		// Grab the CompactMachines entry from the player NBT data
		NBTTagCompound cmNBT = null;
		if (playerNBT.hasKey(Reference.MOD_ID)) {
			cmNBT = playerNBT.getCompoundTag(Reference.MOD_ID);
		}

		int targetDimension = 0;
		double targetX;
		double targetY;
		double targetZ;

		if (cmNBT != null && cmNBT.hasKey("oldPosX")) {
			// First try to grab the original position by looking at the CompactMachines NBT Tag
			targetDimension = cmNBT.getInteger("oldDimension");
			targetX = cmNBT.getDouble("oldPosX");
			targetY = cmNBT.getDouble("oldPosY");
			targetZ = cmNBT.getDouble("oldPosZ");
		} else if(playerNBT.hasKey("oldDimension") && playerNBT.getInteger("oldDimension") != ConfigurationHandler.dimensionId) {
			// Backwards compatibility - but these values are also being set by RandomThings
			// A problem exists in two cases:
			// a) A player entered the SpiritDimension from the CM dimension, RandomThings would set the oldDimension to the CM dimension
			// b) A player entered the CM dimension from the SpectreDimension, CM did previously set the oldDimension to the SpectreDimension
			// In both cases the player gets trapped in a loop between the two dimensions and has no way of getting back to the overworld
			// We want to allow backwards compatibility with our old settings so players in a CM during an update to a version containing this commit
			// would still be trapped in the two dimensions. We can break this cycle by not allowing to get back to another CM using the
			// old system.
			// That's because CM never writes its own dimension into the oldDimension tag, only RandomThings would do that.
			targetDimension = playerNBT.getInteger("oldDimension");
			targetX = playerNBT.getDouble("oldPosX");
			targetY = playerNBT.getDouble("oldPosY");
			targetZ = playerNBT.getDouble("oldPosZ");
		} else {
			ChunkCoordinates cc = MinecraftServer.getServer().worldServerForDimension(0).provider.getRandomizedSpawnPoint();
			targetX = cc.posX;
			targetY = cc.posY;
			targetZ = cc.posZ;
		}

		MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(player, targetDimension, new TeleporterCM(MinecraftServer.getServer().worldServerForDimension(targetDimension)));
		player.setPositionAndUpdate(targetX, targetY, targetZ);
	}

	public static void teleportPlayerBack(EntityPlayerMP player) {
		NBTTagCompound playerNBT = player.getEntityData();
		if (playerNBT.hasKey("coordHistory")) {
			NBTTagList coordHistory = playerNBT.getTagList("coordHistory", 10);
			if (coordHistory.tagCount() == 0) {
				// No coord history so far, teleport back to overworld
				TeleportTools.teleportPlayerOutOfMachineDimension(player);
			} else {
				// Remove the last tag, then teleport to the new last
				coordHistory.removeTag(coordHistory.tagCount() - 1);
				if (coordHistory.tagCount() == 0) {
					TeleportTools.teleportPlayerOutOfMachineDimension(player);
					return;
				}

				int coord = coordHistory.getCompoundTagAt(coordHistory.tagCount() - 1).getInteger("coord");
				TeleportTools.teleportPlayerToCoords(player, coord, true);
			}
		} else {
			// No coord history on the player yet - teleport him out of there.
			TeleportTools.teleportPlayerOutOfMachineDimension(player);
		}
	}

	private static double[] findBestSpawnLocation(WorldServer machineWorld, int coord) {
		int size = Reference.getBoxSize(CompactMachines.instance.machineHandler.getRoomSize(coord));

		int posX1 = coord * ConfigurationHandler.cubeDistance + 1;
		int posY1 = 40 + 1;
		int posZ1 = 1;

		int posX2 = coord * ConfigurationHandler.cubeDistance + size - 1;
		int posY2 = 40 + size - 1;
		int posZ2 = size - 1;

		int minX = Math.min(posX1, posX2);
		int minY = Math.min(posY1, posY2);
		int minZ = Math.min(posZ1, posZ2);

		int maxX = Math.max(posX1, posX2);
		int maxY = Math.max(posY1, posY2) - 1;
		int maxZ = Math.max(posZ1, posZ2);

		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					if (machineWorld.isAirBlock(x, y, z) && machineWorld.isAirBlock(x, y + 1, z)) {
						return new double[] { x + 0.5, y + 0.5, z + 0.5 };
					}
				}
			}
		}

		return null;
	}

	public static void checkPlayerPositions() {
		World worldObj = CompactMachines.instance.machineHandler.getWorld();
		if (worldObj.getTotalWorldTime() % 10 == 0 && ConfigurationHandler.keepPlayersInsideOfRooms) {
			for (int i = 0; i < worldObj.playerEntities.size(); i++) {
				if (worldObj.playerEntities.get(i) instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) worldObj.playerEntities.get(i);
					if (player.capabilities.isCreativeMode && PlayerUtils.isPlayerOpped(player)) {
						// Opped players in creative mode are actually allowed to leave the rooms
						continue;
					}
					int lastCoord = PlayerUtils.getPlayerCoords(player);
					if (lastCoord == -1) {
						// We don't know where the player is atm :(
						continue;
					}

					if (!CompactMachines.instance.machineHandler.hasRoomSize(lastCoord)) {
						// We sadly don't know the size of the room the player is in. Skipping.
						// This automatically changes once any player enters the cube again.
						continue;
					}

					int roomSize = Reference.getBoxSize(CompactMachines.instance.machineHandler.getRoomSize(lastCoord));

					AxisAlignedBB bb = CubeTools.getBoundingBoxForCube(lastCoord, roomSize);
					if (!bb.isVecInside(Vec3.createVectorHelper(player.posX, player.posY, player.posZ))) {
						teleportPlayerToCoords((EntityPlayerMP) player, lastCoord, true);

						// Add potion effects for 200 ticks
						player.addPotionEffect(new PotionEffect(2, 200, 5, false));	// Slowness
						player.addPotionEffect(new PotionEffect(9, 200, 5, false)); // Nausea
					}
				}
			}
		}
	}
}
