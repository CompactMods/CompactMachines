package dev.compactmods.machines.data.persistent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.codec.NbtListCollector;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Holds information on the external points of a machine, ie the actual machine blocks.
 */
public class CompactMachineData extends SavedData {

    /**
     * File storage name.
     */
    public final static String DATA_NAME = CompactMachines.MOD_ID + "_machines";

    /**
     * Specifies locations of machines in-world, outside of the compact world.
     * This is used for things like spawn lookups, tunnel handling, and forced ejections.
     */
    public Map<Integer, MachineData> data;

    public CompactMachineData() {
        data = new HashMap<>();
    }

    @Nullable
    public static CompactMachineData get(MinecraftServer server) {
        ServerLevel compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionDataStorage sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(CompactMachineData::fromNbt, CompactMachineData::new, DATA_NAME);
    }

    public static CompactMachineData fromNbt(CompoundTag nbt) {
        CompactMachineData machines = new CompactMachineData();
        if(nbt.contains("locations")) {
            ListTag nbtLocations = nbt.getList("locations", Tag.TAG_COMPOUND);
            nbtLocations.forEach(nbtLoc -> {
                DataResult<MachineData> res = MachineData.CODEC.parse(NbtOps.INSTANCE, nbtLoc);
                res.resultOrPartial(err -> {
                    CompactMachines.LOGGER.error("Error while processing machine data: " + err);
                }).ifPresent(machineInfo -> machines.data.put(machineInfo.machineId, machineInfo));
            });
        }

        return machines;
    }

    @Override
    @Nonnull
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        if(!data.isEmpty()) {
            ListTag nbtLocations = data.values()
                    .stream()
                    .map(entry -> {
                        DataResult<Tag> nbtRes = MachineData.CODEC.encodeStart(NbtOps.INSTANCE, entry);
                        return nbtRes.resultOrPartial(err -> {
                            CompactMachines.LOGGER.error("Error serializing machine data: " + err);
                        });

                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(NbtListCollector.toNbtList());

            nbt.put("locations", nbtLocations);
        }

        return nbt;
    }

    public boolean isPlaced(Integer machineId) {
        return data.containsKey(machineId);
    }

    public void setMachineLocation(int machineId, DimensionalPosition position) {
        // TODO - Packet/Event for machine changing external location (tunnels)
        if(data.containsKey(machineId)) {
            data.get(machineId).setLocation(position);
        } else {
            data.put(machineId, new MachineData(machineId, position));
        }

        this.setDirty();
    }

    public Optional<DimensionalPosition> getMachineLocation(int machineId) {
        if(!data.containsKey(machineId))
            return Optional.empty();

        MachineData machineData = this.data.get(machineId);
        return Optional.ofNullable(machineData.location);
    }

    public static class MachineData {
        private final int machineId;
        public DimensionalPosition location;

        public static final Codec<MachineData> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("machine").forGetter(MachineData::getMachineId),
                DimensionalPosition.CODEC.fieldOf("location").forGetter(MachineData::getLocation)
        ).apply(i, MachineData::new));

        public MachineData(int machineId, DimensionalPosition location) {
            this.machineId = machineId;
            this.location = location;
        }

        public int getMachineId() {
            return this.machineId;
        }

        public DimensionalPosition getLocation() {
            return this.location;
        }

        public MachineData setLocation(DimensionalPosition position) {
            this.location = position;
            return this;
        }
    }
}
