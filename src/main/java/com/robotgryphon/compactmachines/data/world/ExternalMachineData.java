package com.robotgryphon.compactmachines.data.world;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.codec.NbtListCollector;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds information on the external points of a machine, ie the actual machine blocks.
 */
public class ExternalMachineData extends WorldSavedData {

    /**
     * File storage name.
     */
    public final static String DATA_NAME = "machines_external";

    /**
     * Used for performing reverse lookups on which machine internals are mapped
     * to their external points. Effectively a cache for machineMapping.
     */
    public Map<ChunkPos, Set<Integer>> reverseMappingCache;

    /**
     * Maps an external machine point to an internal machine chunk position.
     * The inside of a machine may have multiple points accessing it in the future,
     * due to advanced tunnels and fancier integrations.
     */
    public Map<Integer, ChunkPos> machineMapping;

    /**
     * Specifies locations of machines in-world, outside of the compact world.
     * This is used for things like spawn lookups, tunnel handling, and forced ejections.
     */
    public Map<Integer, DimensionalPosition> machineLocations;

    public ExternalMachineData() {
        super(DATA_NAME);
        machineMapping = new HashMap<>();
        machineLocations = new HashMap<>();
        reverseMappingCache = new HashMap<>();
    }

    @Nullable
    public static ExternalMachineData get(MinecraftServer server) {
        ServerWorld compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionSavedDataManager sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(ExternalMachineData::new, DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        if(nbt.contains("mapping")) {
            ListNBT mapping = nbt.getList("mapping", Constants.NBT.TAG_COMPOUND);
            mapping.forEach(map -> {
                CompoundNBT mapEntry = (CompoundNBT) map;
                ChunkPos cp = new ChunkPos(
                        mapEntry.getInt("chunkX"),
                        mapEntry.getInt("chunkZ")
                );

                int id = mapEntry.getInt("id");

                // add to reverse lookup mapping
                machineMapping.put(id, cp);
                reverseMappingCache.putIfAbsent(cp, new HashSet<>());
                reverseMappingCache.get(cp).add(id);
            });
        }

        if(nbt.contains("locations")) {
            ListNBT nbtLocations = nbt.getList("locations", Constants.NBT.TAG_COMPOUND);
            nbtLocations.forEach(nbtLoc -> {
                CompoundNBT loc = (CompoundNBT) nbtLoc;
                int machine = loc.getInt("machine");
                CompoundNBT locationNbt = loc.getCompound("location");
                DimensionalPosition dimLoc = DimensionalPosition.fromNBT(locationNbt);

                machineLocations.put(machine, dimLoc);
            });
        }
    }

    @Override
    @Nonnull
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        if(!machineMapping.isEmpty()) {
            ListNBT list = machineMapping.entrySet()
                    .stream()
                    .map((i) -> {
                        ChunkPos cPos = i.getValue();
                        CompoundNBT entry = new CompoundNBT();
                        {
                            entry.putInt("id", i.getKey());
                            entry.putInt("chunkX", cPos.x);
                            entry.putInt("chunkZ", cPos.z);
                        }

                        return entry;
                    }).collect(NbtListCollector.toNbtList());

            nbt.put("mapping", list);
        }

        if(!machineLocations.isEmpty()) {
            ListNBT nbtLocations = machineLocations.entrySet().stream()
                    .map(l -> {
                        int machine = l.getKey();
                        DimensionalPosition location = l.getValue();

                        CompoundNBT entry = new CompoundNBT();
                        {
                            entry.putInt("machine", machine);
                            entry.put("location", location.serializeNBT());
                        }

                        return entry;
                    }).collect(NbtListCollector.toNbtList());

            nbt.put("locations", nbtLocations);
        }

        return nbt;
    }

    public Set<Integer> getExternalMachineIDs(ChunkPos internal) {
        if(!reverseMappingCache.containsKey(internal)) {
            // If we hit this, we have an internal position that somehow didn't map to the external points
            rebuildReverseMapCache(internal);
        }

        return reverseMappingCache.get(internal);
    }

    private void rebuildReverseMapCache(ChunkPos internal) {
        // Rebuild the cache here
        Set<Integer> remapped = machineMapping
                .entrySet().stream()
                .filter(e -> e.getValue().equals(internal))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        reverseMappingCache.put(internal, remapped);
    }

    public Optional<ChunkPos> getChunkLocation(int machineId) {
        return Optional.ofNullable(machineMapping.get(machineId));
    }

    public boolean isPlaced(Integer machineId) {
        return machineLocations.containsKey(machineId);
    }

    public void setMachineLocation(int machineId, DimensionalPosition position) {
        // TODO - Packet/Event for machine changing external location (tunnels)
        machineLocations.put(machineId, position);
        this.setDirty();
    }

    @Nullable
    public DimensionalPosition getMachineLocation(int machineId) {
        return machineLocations.get(machineId);
    }

    public Set<DimensionalPosition> getExternalMachineLocations(ChunkPos inside) {
        Set<Integer> externIDs = getExternalMachineIDs(inside);

        return machineLocations.entrySet()
                .stream()
                .filter(in -> externIDs.contains(in.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }
}
