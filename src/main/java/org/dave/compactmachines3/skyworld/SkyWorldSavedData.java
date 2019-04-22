package org.dave.compactmachines3.skyworld;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.utility.Logz;

import java.util.*;

public class SkyWorldSavedData extends WorldSavedData {
    private static final String SAVED_DATA_NAME = "CompactSkiesSavedData";

    public static SkyWorldSavedData instance;

    private Set<UUID> hubMachineOwners;
    private Set<UUID> startingInventoryReceivers;
    private Map<UUID, Integer> homeOwnerMapping;

    public SkyWorldSavedData(String name) {
        super(name);

        hubMachineOwners = new HashSet<>();
        startingInventoryReceivers = new HashSet<>();
        homeOwnerMapping = new HashMap<>();
    }

    public boolean isHubMachineOwner(EntityPlayer player) {
        return hubMachineOwners.contains(player.getUniqueID());
    }

    public void setHomeOwner(EntityPlayer player, int coords) {
        homeOwnerMapping.put(player.getUniqueID(), coords);
        hubMachineOwners.add(player.getUniqueID());
        this.markDirty();
    }

    public boolean hasSkyworldHome(EntityPlayer player) {
        return homeOwnerMapping.containsKey(player.getUniqueID());
    }

    public int getSkyworldHome(EntityPlayer player) {
        return homeOwnerMapping.get(player.getUniqueID());
    }

    public boolean hasReceivedStartingInventory(EntityPlayer player) {
        return startingInventoryReceivers.contains(player.getUniqueID());
    }

    public void addToStartingInventoryReceiverSet(EntityPlayer player) {
        startingInventoryReceivers.add(player.getUniqueID());
        this.markDirty();
    }

    @SubscribeEvent
    public static void loadWorld(WorldEvent.Load event) {
        World world = event.getWorld();
        if (world.isRemote || !(world.getWorldType() instanceof SkyWorldType)) {
            return;
        }

        SkyWorldSavedData data = (SkyWorldSavedData) world.getMapStorage().getOrLoadData(SkyWorldSavedData.class, SAVED_DATA_NAME);
        if(data == null) {
            data = new SkyWorldSavedData(SAVED_DATA_NAME);
            data.markDirty();
        }

        Logz.info("Compact Skies Machine Owners: %d", data.hubMachineOwners.size());

        instance = data;
        world.getMapStorage().setData(SAVED_DATA_NAME, data);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        hubMachineOwners.clear();
        if(nbt.hasKey("hubMachineOwners")) {
            NBTTagList tagList = nbt.getTagList("hubMachineOwners", Constants.NBT.TAG_COMPOUND);
            for(NBTBase baseUUID : tagList) {
                NBTTagCompound compoundUUID = (NBTTagCompound)baseUUID;
                UUID uuid = compoundUUID.getUniqueId("");

                hubMachineOwners.add(uuid);
            }
        }

        startingInventoryReceivers.clear();
        if(nbt.hasKey("startingInventoryReceivers")) {
            NBTTagList tagList = nbt.getTagList("startingInventoryReceivers", Constants.NBT.TAG_COMPOUND);
            for(NBTBase baseUUID : tagList) {
                NBTTagCompound compoundUUID = (NBTTagCompound)baseUUID;
                UUID uuid = compoundUUID.getUniqueId("");

                startingInventoryReceivers.add(uuid);
            }
        }

        homeOwnerMapping.clear();
        if(nbt.hasKey("homeOwnerMapping")) {
            NBTTagCompound tagCompound = nbt.getCompoundTag("homeOwnerMapping");
            for(String key : tagCompound.getKeySet()) {
                int coord = Integer.parseInt(key);
                NBTTagCompound innerCompound = tagCompound.getCompoundTag(key);
                UUID owner = innerCompound.getUniqueId("");
                homeOwnerMapping.put(owner, coord);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList hubMachineOwnersTagList = new NBTTagList();
        for(UUID uuid : hubMachineOwners) {
            NBTTagCompound compoundUUID = new NBTTagCompound();
            compoundUUID.setUniqueId("", uuid);
            hubMachineOwnersTagList.appendTag(compoundUUID);
        }

        compound.setTag("hubMachineOwners", hubMachineOwnersTagList);

        NBTTagList startingInventoryReceiversTagList = new NBTTagList();
        for(UUID uuid : startingInventoryReceivers) {
            NBTTagCompound compoundUUID = new NBTTagCompound();
            compoundUUID.setUniqueId("", uuid);
            startingInventoryReceiversTagList.appendTag(compoundUUID);
        }

        compound.setTag("startingInventoryReceivers", startingInventoryReceiversTagList);

        NBTTagCompound homeMapping = new NBTTagCompound();
        for(UUID owner : homeOwnerMapping.keySet()) {
            NBTTagCompound innerKey = new NBTTagCompound();
            innerKey.setUniqueId("", owner);

            int coords = homeOwnerMapping.get(owner);
            homeMapping.setTag("" + coords, innerKey);
        }

        compound.setTag("homeOwnerMapping", homeMapping);

        return compound;
    }
}
