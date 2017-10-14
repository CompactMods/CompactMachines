package org.dave.compactmachines3.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.world.tools.StructureTools;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.utility.Logz;

import java.util.HashMap;
import java.util.UUID;

public class WorldSavedDataMachines extends WorldSavedData {

    public int nextCoord = 0;
    public HashMap<Integer, double[]> spawnPoints = new HashMap<>();
    public HashMap<Integer, HashMap<EnumFacing, BlockPos>> tunnels = new HashMap<>();
    public HashMap<Integer, DimensionBlockPos> machinePositions = new HashMap<>();
    public HashMap<UUID, Integer> bedCoords = new HashMap<>();

    public static WorldSavedDataMachines INSTANCE;

    public void setBedCoords(EntityPlayer player) {
        int coords = StructureTools.getCoordsForPos(new BlockPos(player.posX, player.posY, player.posZ));
        bedCoords.put(player.getUniqueID(), coords);
        this.markDirty();
    }

    public int getBedCoors(EntityPlayer player) {
        return bedCoords.getOrDefault(player.getUniqueID(), -1);
    }

    // TODO: Cleanup this class. It's all over the place.
    public DimensionBlockPos getMachinePosition(int coord) {
        return machinePositions.get(coord);
    }

    public void addMachinePosition(int coord, BlockPos pos, int dimension) {
        machinePositions.put(coord, new DimensionBlockPos(pos, dimension));
        Logz.debug("Adding machine position: coords=%d, pos=%s, dimension=%d", coord, pos, dimension);
        this.markDirty();
    }

    private void addSpawnPoint(int coord, double[] destination) {
        if(destination.length != 3) {
            Logz.warn("Trying to set spawn point with invalid double[]=%s", destination);
            return;
        }

        spawnPoints.put(coord, destination);
        Logz.debug("Setting spawn point: coords=%d, x=%.2f, y=%.2f, z=%.2f", coord, destination[0], destination[1], destination[2]);
        this.markDirty();
    }

    public void addSpawnPoint(int coord, double x, double y, double z) {
        addSpawnPoint(coord, new double[]{x, y, z});
    }

    public void removeMachinePosition(int coord) {
        machinePositions.remove(coord);
        Logz.debug("Removing machine position by coord: coords=%d", coord);
        this.markDirty();
    }

    // TODO: We might want to move this to a different class?
    public static int reserveMachineId() {
        int val = WorldSavedDataMachines.INSTANCE.nextCoord;
        WorldSavedDataMachines.INSTANCE.nextCoord++;
        WorldSavedDataMachines.INSTANCE.markDirty();
        return val;
    }

    public WorldSavedDataMachines(String name) {
        super(name);
    }

    @SubscribeEvent
    public static void loadWorld(WorldEvent.Load event) {
        if(event.getWorld().isRemote || event.getWorld().provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        Logz.info("Loading saved data for machine world");
        WorldSavedDataMachines wsd = (WorldSavedDataMachines)event.getWorld().getMapStorage().getOrLoadData(WorldSavedDataMachines.class, "WorldSavedDataMachines");
        if(wsd == null) {
            wsd = new WorldSavedDataMachines("WorldSavedDataMachines");
            wsd.markDirty();
        }

        Logz.info(" > %d spawn points", wsd.spawnPoints.size());
        Logz.info(" > Next machine id: %d", wsd.nextCoord);
        Logz.info(" > Players with beds in CM dimension: %d", wsd.bedCoords.size());

        WorldSavedDataMachines.INSTANCE = wsd;
        event.getWorld().getMapStorage().setData("WorldSavedDataMachines", wsd);
    }

    public void removeTunnel(BlockPos pos) {
        int coords = StructureTools.getCoordsForPos(pos);
        HashMap<EnumFacing, BlockPos> sideMapping = tunnels.get(coords);
        if(sideMapping == null) {
            return;
        }

        EnumFacing sideToRemove = null;
        for(EnumFacing side : sideMapping.keySet()) {
            if(sideMapping.get(side).equals(pos)) {
                sideToRemove = side;
                break;
            }
        }

        if(sideToRemove != null) {
            Logz.debug("Removing tunnel mapping by blockpos: pos=%s --> coords=%d, side=%s", pos, coords, sideToRemove);
            sideMapping.remove(sideToRemove);
        }
        this.markDirty();
    }

    public void removeTunnel(BlockPos position, EnumFacing side) {
        int coords = StructureTools.getCoordsForPos(position);
        HashMap<EnumFacing, BlockPos> sideMapping = tunnels.get(coords);
        if(sideMapping == null) {
            return;
        }

        Logz.debug("Removing tunnel mapping by pos+side: coords=%d, side=%s", coords, side);
        sideMapping.remove(side);
        this.markDirty();
    }

    public void addTunnel(BlockPos position, EnumFacing side) {
        this.addTunnel(position, side, false);
    }

    private void addTunnel(BlockPos position, EnumFacing side, boolean isLoading) {
        int coords = StructureTools.getCoordsForPos(position);

        HashMap<EnumFacing, BlockPos> sideMapping = tunnels.get(coords);
        if(sideMapping == null) {
            sideMapping = new HashMap<>();
            tunnels.put(coords, sideMapping);
        }

        sideMapping.put(side, position);
        Logz.debug("Adding tunnel mapping: side=%s, pos=%s --> coords=%d", side, position, coords);

        if(!isLoading) {
            this.markDirty();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("nextMachineCoord", nextCoord);

        NBTTagCompound bedCoordsMap = new NBTTagCompound();
        for(UUID playerId : bedCoords.keySet()) {
            int coords = bedCoords.get(playerId);
            bedCoordsMap.setInteger(playerId.toString(), coords);
        }

        NBTTagList spawnPointList = new NBTTagList();
        for(int coords : spawnPoints.keySet()) {
            double[] positions = spawnPoints.get(coords);

            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("coords", coords);
            tag.setDouble("x", positions[0]);
            tag.setDouble("y", positions[1]);
            tag.setDouble("z", positions[2]);
            spawnPointList.appendTag(tag);
        }

        NBTTagList tunnelList = new NBTTagList();
        for(int coords: tunnels.keySet()) {
            HashMap<EnumFacing, BlockPos> sideMappings = tunnels.get(coords);

            for(EnumFacing side : sideMappings.keySet()) {
                BlockPos position = sideMappings.get(side);

                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("side", side.getIndex());
                tag.setInteger("x", position.getX());
                tag.setInteger("y", position.getY());
                tag.setInteger("z", position.getZ());
                tunnelList.appendTag(tag);
            }
        }

        NBTTagList machineList = new NBTTagList();
        for(int coords: machinePositions.keySet()) {
            DimensionBlockPos dimpos = machinePositions.get(coords);
            BlockPos position = dimpos.getBlockPos();

            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("coords", coords);
            tag.setInteger("x", position.getX());
            tag.setInteger("y", position.getY());
            tag.setInteger("z", position.getZ());
            tag.setInteger("dim", dimpos.getDimension());
            machineList.appendTag(tag);
        }

        compound.setTag("spawnpoints", spawnPointList);
        compound.setTag("tunnels", tunnelList);
        compound.setTag("machines", machineList);
        compound.setTag("bedcoords", bedCoordsMap);
        return compound;
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        nextCoord = nbt.getInteger("nextMachineCoord");

        if(nbt.hasKey("bedcoords")) {
            bedCoords.clear();
            NBTTagCompound bedCoordsMap = nbt.getCompoundTag("bedcoords");
            for(String uuidString : bedCoordsMap.getKeySet()) {
                UUID uuid = UUID.fromString(uuidString);
                int coords = bedCoordsMap.getInteger(uuidString);
                bedCoords.put(uuid, coords);
            }
        }

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

        if(nbt.hasKey("tunnels")) {
            tunnels.clear();
            NBTTagList tagList = nbt.getTagList("tunnels", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);

                BlockPos position = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
                EnumFacing side = EnumFacing.getFront(tag.getInteger("side"));

                this.addTunnel(position, side, true);
            }
        }

        if(nbt.hasKey("machines")) {
            machinePositions.clear();
            NBTTagList tagList = nbt.getTagList("machines", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                NBTTagCompound tag = tagList.getCompoundTagAt(i);

                BlockPos position = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
                machinePositions.put(tag.getInteger("coords"), new DimensionBlockPos(position, tag.getInteger("dim")));
            }
        }
    }
}
