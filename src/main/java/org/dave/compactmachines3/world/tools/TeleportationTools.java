package org.dave.compactmachines3.world.tools;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.network.MessageMachinePositions;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.schema.SchemaRegistry;
import org.dave.compactmachines3.skyworld.SkyWorldSavedData;
import org.dave.compactmachines3.skyworld.SkyWorldType;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.TeleporterMachines;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class TeleportationTools {
    public static boolean tryToEnterMachine(EntityPlayer player, TileEntityMachine machine) {
        BlockPos pos = machine.getPos();
        World world = machine.getWorld();

        if(machine.isInsideItself()) {
            return false;
        }

        if(!machine.isAllowedToEnter(player)) {
            player.sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.not_permitted_to_enter"), true);
            return false;
        }

        // Prevent players from claiming more than one machine in the SkyWorld Machine Hub
        if(!machine.hasOwner() && world.getWorldType() instanceof SkyWorldType) {
            boolean playerHasHubMachine = SkyWorldSavedData.instance.isHubMachineOwner(player);
            if(playerHasHubMachine) {
                player.sendStatusMessage(new TextComponentTranslation("hint.compactmachines3.skyworld.only_one_machine_claim"), true);
                return false;
            }
        }

        boolean isNew = machine.id == -1;
        machine.initStructure();

        WorldSavedDataMachines.getInstance().addMachinePosition(machine.id, pos, world.provider.getDimension());

        if (isNew) // A new grid position was chosen if the id had not yet been set before the structure init, have to update for clients
            MessageMachinePositions.updateClientMachinePositions();

        TeleportationTools.teleportPlayerToMachine((EntityPlayerMP) player, machine);
        StructureTools.setBiomeForMachineId(machine.id, world.getBiome(pos));

        if(!machine.hasOwner() || "Unknown".equals(machine.getOwnerName())) {
            machine.setOwner(player);
            machine.markDirty();

            if(world.getWorldType() instanceof SkyWorldType) {
                SkyWorldSavedData.instance.setHomeOwner(player, machine.id);
            }
        }

        return true;
    }

    public static void teleportToSkyworldHome(EntityPlayer player) {
        if(!SkyWorldSavedData.instance.hasSkyworldHome(player)) {
            return;
        }

        int id = SkyWorldSavedData.instance.getSkyworldHome(player);
        DimensionBlockPos pos = WorldSavedDataMachines.getInstance().machinePositions.get(id);
        if(pos == null) {
            return;
        }

        BlockPos spawnPoint = pos.getBlockPos();
        player.setPositionAndUpdate(spawnPoint.getX() + 0.5d, spawnPoint.getY() + 1.2d, spawnPoint.getZ() + 0.5d);
    }

    public static int getLastKnownRoomId(EntityPlayer player, boolean getFirst) {
        NBTTagCompound playerNBT = player.getEntityData();
        if(!playerNBT.hasKey("compactmachines3-idHistory") && /* Legacy */ !playerNBT.hasKey("compactmachines3-coordHistory")) {
            return -1;
        }

        NBTTagList idHistory = playerNBT.getTagList("compactmachines3-idHistory", 10);
        if (idHistory.isEmpty()) {
            idHistory = playerNBT.getTagList("compactmachines3-coordHistory", 10); // Legacy
        }
        if(idHistory.isEmpty()) {
            return -1;
        }

        NBTTagCompound nbt = idHistory.getCompoundTagAt(getFirst ? 0 : idHistory.tagCount() - 1);
        return nbt.hasKey("id", 3) ? nbt.getInteger("id") : /* Legacy */ nbt.getInteger("coords");
    }

    public static void teleportPlayerToMachine(EntityPlayerMP player, int id, boolean isReturning) {
        if (id == -1)
            return;

        NBTTagCompound playerNBT = player.getEntityData();
        if (player.dimension != ConfigurationHandler.Settings.dimensionId) {
            playerNBT.setInteger("compactmachines3-oldDimension", player.dimension);
            playerNBT.setDouble("compactmachines3-oldPosX", player.posX);
            playerNBT.setDouble("compactmachines3-oldPosY", player.posY);
            playerNBT.setDouble("compactmachines3-oldPosZ", player.posZ);

            WorldServer machineWorld = DimensionTools.getServerMachineWorld();
            player.addExperienceLevel(0);
            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            playerList.transferPlayerToDimension(player, ConfigurationHandler.Settings.dimensionId, new TeleporterMachines(machineWorld));

            playerNBT.removeTag("compactmachines3-idHistory");
            playerNBT.removeTag("compactmachines3-coordHistory"); // Legacy
        }

        if(!isReturning) {
            addMachineIdToHistory(id, playerNBT);
        }

        TileEntityMachine machine = WorldSavedDataMachines.getInstance().getMachine(id);
        if (machine == null)
            return;

        if(machine.hasNewSchema()) {
            Schema schema = SchemaRegistry.instance.getSchema(machine.getSchemaName());
            if(schema == null) {
                CompactMachines3.logger.warn("Unknown schema used by Compact Machine @ {}", WorldSavedDataMachines.getInstance().getMachineBlockPosition(id));
            } else {
                StructureTools.restoreSchema(schema, id);

                Vec3d adjustedSpawnPosition = schema.getSpawnPosition().add(new Vec3d(WorldSavedDataMachines.getInstance().getMachineRoomPosition(id)));
                WorldSavedDataMachines.getInstance().addSpawnPoint(machine.id, adjustedSpawnPosition);
                machine.setSchema(null);
                machine.markDirty();
            }
        }

        Vec3d destination = WorldSavedDataMachines.getInstance().spawnPoints.get(id);
        player.setPositionAndUpdate(destination.x, destination.y, destination.z);
    }

    public static void addMachineIdToHistory(int id, EntityPlayer player) {
        addMachineIdToHistory(id, player.getEntityData());
    }

    public static void addMachineIdToHistory(int id, NBTTagCompound playerNBT) {
        NBTTagList idHistory = playerNBT.getTagList("compactmachines3-idHistory", 10);
        if (idHistory.isEmpty()) {
            idHistory = playerNBT.getTagList("compactmachines3-coordHistory", 10); // Legacy
        }

        NBTTagCompound toAppend = new NBTTagCompound();
        toAppend.setInteger("id", id);

        idHistory.appendTag(toAppend);
        playerNBT.setTag("compactmachines3-idHistory", idHistory);
    }

    public static void teleportPlayerOutOfMachineDimension(EntityPlayerMP player) {
        PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

        NBTTagCompound playerNBT = player.getEntityData();
        if (playerNBT.hasKey("compactmachines3-oldPosX")) {
            int oldDimension = playerNBT.getInteger("compactmachines3-oldDimension");
            double oldPosX = playerNBT.getDouble("compactmachines3-oldPosX");
            double oldPosY = playerNBT.getDouble("compactmachines3-oldPosY");
            double oldPosZ = playerNBT.getDouble("compactmachines3-oldPosZ");

            player.addExperienceLevel(0);
            playerList.transferPlayerToDimension(player, oldDimension, new TeleporterMachines(DimensionTools.getWorldServerForDimension(oldDimension)));
            player.setPositionAndUpdate(oldPosX, oldPosY, oldPosZ);
        } else {
            int firstId = getLastKnownRoomId(player, true);
            playerNBT.removeTag("compactmachines3-idHistory");
            playerNBT.removeTag("compactmachines3-coordHistory");
            if (firstId == -1) {
                firstId = StructureTools.getIdForPos(new BlockPos(player.posX, player.posY, player.posZ));
            }

            DimensionBlockPos pos = WorldSavedDataMachines.getInstance().machinePositions.get(firstId);

            BlockPos startPoint;
            int dimension;

            if(pos != null) {
                // The machine exists and we know its position
                dimension = pos.getDimension();
                startPoint = pos.getBlockPos();
            } else {
                // We have no idea -> use the world spawn instead
                dimension = 0;
                startPoint = DimensionTools.getWorldServerForDimension(0).provider.getRandomizedSpawnPoint();
            }

            WorldServer world = DimensionTools.getWorldServerForDimension(dimension);
            BlockPos spawnPoint = pos != null ? getValidSpawnLocation(world, startPoint) : startPoint;

            playerList.transferPlayerToDimension(player, dimension, new TeleporterMachines(world));
            player.setPositionAndUpdate(spawnPoint.getX() + 0.5d, spawnPoint.getY() + 0.2d, spawnPoint.getZ() + 0.5d);

        }
    }

    public static BlockPos getValidSpawnLocation(WorldServer world, BlockPos start) {
        // Spiral outwards
        int blocksToCheck = (5*5 - 1) * 3;
        int radius = 1;
        int checked = 0;
        while(checked < blocksToCheck) {
            for (int y = -1; y < 2; y++) {
                for (int q = -radius + 1; q <= radius && checked < blocksToCheck; q++) {
                    BlockPos check = start.add(radius, y, q);
                    if (world.isAirBlock(check) && world.isAirBlock(check.up()) && !world.isAirBlock(check.down())) {
                        return check;
                    }
                    checked++;
                }
                for (int q = radius - 1; q >= -radius && checked < blocksToCheck; q--) {
                    BlockPos check = start.add(q, y, radius);
                    if (world.isAirBlock(check) && world.isAirBlock(check.up()) && !world.isAirBlock(check.down())) {
                        return check;
                    }
                    checked++;
                }
                for (int q = radius - 1; q >= -radius && checked < blocksToCheck; q--) {
                    BlockPos check = start.add(-radius, y, q);
                    if (world.isAirBlock(check) && world.isAirBlock(check.up()) && !world.isAirBlock(check.down())) {
                        return check;
                    }
                    checked++;
                }
                for (int q = -radius + 1; q <= radius && checked < blocksToCheck; q++) {
                    BlockPos check = start.add(q, y, -radius);
                    if (world.isAirBlock(check) && world.isAirBlock(check.up()) && !world.isAirBlock(check.down())) {
                        return check;
                    }
                    checked++;
                }
            }
        }

        return start;
    }


    public static void teleportPlayerToMachine(EntityPlayerMP player, TileEntityMachine machine) {
        teleportPlayerToMachine(player, machine.id, false);
    }

    public static void teleportPlayerOutOfMachine(EntityPlayerMP player) {
        NBTTagCompound playerNBT = player.getEntityData();
        if (!playerNBT.hasKey("compactmachines3-idHistory") && /* Legacy */ !playerNBT.hasKey("compactmachines3-coordHistory")) {
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        NBTTagList idHistory = playerNBT.getTagList("compactmachines3-idHistory", 10);
        if (idHistory.isEmpty()) {
            idHistory = playerNBT.getTagList("compactmachines3-coordHistory", 10); // Legacy
        }
        if(idHistory.isEmpty()) {
            // No id history, teleport back to overworld
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        // Remove the last tag, then teleport to the new last
        idHistory.removeTag(idHistory.tagCount()-1);

        // No id history -> Back to the overworld
        if(idHistory.tagCount() == 0) {
            teleportPlayerOutOfMachineDimension(player);
            return;
        }

        NBTTagCompound lastTag = idHistory.getCompoundTagAt(idHistory.tagCount() - 1);
        int id = lastTag.hasKey("id", 3) ? lastTag.getInteger("id") : /* Legacy */ lastTag.getInteger("coords");
        teleportPlayerToMachine(player, id, true);
    }
}
