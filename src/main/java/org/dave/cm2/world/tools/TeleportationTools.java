package org.dave.cm2.world.tools;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.world.TeleporterMachines;
import org.dave.cm2.world.WorldSavedDataMachines;

public class TeleportationTools {
    private static void teleportPlayerToMachine(EntityPlayerMP player, int coords, boolean isReturning) {
        NBTTagCompound playerNBT = player.getEntityData();
        if (player.dimension != ConfigurationHandler.Settings.dimensionId) {
            playerNBT.setInteger("cm2-oldDimension", player.dimension);
            playerNBT.setDouble("cm2-oldPosX", player.posX);
            playerNBT.setDouble("cm2-oldPosY", player.posY);
            playerNBT.setDouble("cm2-oldPosZ", player.posZ);

            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            playerList.transferPlayerToDimension(player, ConfigurationHandler.Settings.dimensionId, new TeleporterMachines(machineWorld));

            if(playerNBT.hasKey("cm2-coordHistory")) {
                playerNBT.removeTag("cm2-coordHistory");
            }
        }

        if(!isReturning) {
            NBTTagList coordHistory;
            if (playerNBT.hasKey("cm2-coordHistory")) {
                coordHistory = playerNBT.getTagList("cm2-coordHistory", 10);
            } else {
                coordHistory = new NBTTagList();
            }

            NBTTagCompound toAppend = new NBTTagCompound();
            toAppend.setInteger("coord", coords);

            coordHistory.appendTag(toAppend);
            playerNBT.setTag("cm2-coordHistory", coordHistory);
        }

        double[] destination = WorldSavedDataMachines.INSTANCE.spawnPoints.get(coords);
        player.setPositionAndUpdate(destination[0], destination[1], destination[2]);
    }

    private static void teleportPlayerOutOfMachineDimension(EntityPlayerMP player) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

        NBTTagCompound playerNBT = player.getEntityData();
        if (playerNBT.hasKey("cm2-oldPosX")) {
            int oldDimension = playerNBT.getInteger("cm2-oldDimension");
            double oldPosX = playerNBT.getDouble("cm2-oldPosX");
            double oldPosY = playerNBT.getDouble("cm2-oldPosY");
            double oldPosZ = playerNBT.getDouble("cm2-oldPosZ");

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
        if (!playerNBT.hasKey("cm2-coordHistory")) {
            return;
        }

        NBTTagList coordHistory = playerNBT.getTagList("cm2-coordHistory", 10);
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
