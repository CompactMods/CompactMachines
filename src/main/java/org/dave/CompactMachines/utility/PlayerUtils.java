package org.dave.CompactMachines.utility;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

public class PlayerUtils {
	public static boolean isPlayerOpped(EntityPlayer player) {
		return MinecraftServer.getServer().getConfigurationManager().func_152596_g(player.getGameProfile());
	}

	public static int getPlayerCoords(EntityPlayer player) {
		NBTTagCompound playerNBT = player.getEntityData();
		if(!playerNBT.hasKey("coordHistory")) {
			return -1;
		}

		NBTTagList coordHistory = playerNBT.getTagList("coordHistory", 10);
		if(coordHistory.tagCount() == 0) {
			return -1;
		}

		return coordHistory.getCompoundTagAt(coordHistory.tagCount()-1).getInteger("coord");
	}
}
