package org.dave.compactmachines3.world.tools;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.world.TeleporterMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class TeleportationTools {
    public static void teleportPlayerToMachine(EntityPlayerMP player, int coords, boolean isReturning) {
        NBTTagCompound playerNBT = player.getEntityData();
        if (player.dimension != ConfigurationHandler.Settings.dimensionId) {
            playerNBT.setInteger("compactmachines3-oldDimension", player.dimension);
            playerNBT.setDouble("compactmachines3-oldPosX", player.posX);
            playerNBT.setDouble("compactmachines3-oldPosY", player.posY);
            playerNBT.setDouble("compactmachines3-oldPosZ", player.posZ);

            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            playerList.transferPlayerToDimension(player, ConfigurationHandler.Settings.dimensionId, new TeleporterMachines(machineWorld));

            if(playerNBT.hasKey("compactmachines3-coordHistory")) {
                playerNBT.removeTag("compactmachines3-coordHistory");
            }
        }

        if(!isReturning) {
            NBTTagList coordHistory;
            if (playerNBT.hasKey("compactmachines3-coordHistory")) {
                coordHistory = playerNBT.getTagList("compactmachines3-coordHistory", 10);
            } else {
                coordHistory = new NBTTagList();
            }

            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setInteger("coord", coords);

            coordHistory.appendTag(toAppend);
            playerNBT.setTag("compactmachines3-coordHistory", coordHistory);
        }

        double[] destination = WorldSavedDataMachines.INSTANCE.spawnPoints.get(coords);
        player.setPositionAndUpdate(destination[0], destination[1], destination[2]);
    }

    public static void teleportPlayerOutOfMachineDimension(EntityPlayerMP player) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

        NBTTagCompound playerNBT = player.getEntityData();
        if (playerNBT.hasKey("compactmachines3-oldPosX")) {
            int oldDimension = playerNBT.getInteger("compactmachines3-oldDimension");
            double oldPosX = playerNBT.getDouble("compactmachines3-oldPosX");
            double oldPosY = playerNBT.getDouble("compactmachines3-oldPosY");
            double oldPosZ = playerNBT.getDouble("compactmachines3-oldPosZ");

            playerList.transferPlayerToDimension(player, oldDimension, new TeleporterMachines(DimensionTools.getWorldServerForDimension(oldDimension)));
            player.setPositionAndUpdate(oldPosX, oldPosY, oldPosZ);
        } else {
            // TODO: We can do better now, since we know where the Machine block is -> Find a good nearby spawn position
            BlockPos spawnPoint = DimensionTools.getWorldServerForDimension(0).provider.getRandomizedSpawnPoint();

            playerList.transferPlayerToDimension(player, 0, new TeleporterMachines(DimensionTools.getWorldServerForDimension(0)));
            player.setPositionAndUpdate(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
        }
    }


    public static void teleportPlayerToMachine(EntityPlayerMP player, TileEntityMachine machine) {
        if(machine.coords == -1) {
            StructureTools.generateCubeForMachine(machine);

            double[] destination = new double[] {
                    machine.coords * 1024 + 0.5 + machine.getSize().getDimension() / 2,
                    42,
                    0.5 + machine.getSize().getDimension() / 2
            };

            double x = machine.coords * 1024 + 0.5 + machine.getSize().getDimension() / 2;
            double y = 42;
            double z = 0.5 + machine.getSize().getDimension() / 2;
            WorldSavedDataMachines.INSTANCE.addSpawnPoint(machine.coords, x, y, z);
        }

        teleportPlayerToMachine(player, machine.coords, false);
    }

    public static void teleportPlayerOutOfMachine(EntityPlayerMP player) {
        NBTTagCompound playerNBT = player.getEntityData();
        if (!playerNBT.hasKey("compactmachines3-coordHistory")) {
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        NBTTagList coordHistory = playerNBT.getTagList("compactmachines3-coordHistory", 10);
        if(coordHistory.tagCount() == 0) {
            // No id history, teleport back to overworld
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        // Remove the last tag, then teleport to the new last
        coordHistory.removeTag(coordHistory.tagCount()-1);

        // No id history -> Back to the overworld
        if(coordHistory.tagCount() == 0) {
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        int coords = coordHistory.getCompoundTagAt(coordHistory.tagCount()-1).getInteger("coord");
        teleportPlayerToMachine(player, coords, true);
    }
}
