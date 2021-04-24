package com.robotgryphon.compactmachines.data.world;

import com.mojang.serialization.DataResult;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.codec.NbtListCollector;
import com.robotgryphon.compactmachines.data.machine.CompactMachineInternalData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.OperationNotSupportedException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InternalMachineData extends WorldSavedData {
    public static final String DATA_NAME = "machines_internal";

    private Map<ChunkPos, CompactMachineInternalData> machineData;

    public InternalMachineData() {
        super(DATA_NAME);
        machineData = new HashMap<>();
    }

    @Nullable
    public static InternalMachineData get(MinecraftServer server) {
        ServerWorld compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionSavedDataManager sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(InternalMachineData::new, DATA_NAME);
    }

    @Override
    public void load(CompoundNBT nbt) {
        if (nbt.contains("machines")) {
            ListNBT machines = nbt.getList("machines", Constants.NBT.TAG_COMPOUND);
            machines.forEach(machNbt -> {
                DataResult<CompactMachineInternalData> result =
                        CompactMachineInternalData.CODEC.parse(NBTDynamicOps.INSTANCE, machNbt);

                result
                    .resultOrPartial((err) -> CompactMachines.LOGGER.error("Error loading machine data from file: {}", err))
                    .ifPresent(imd -> {
                        ChunkPos chunk = new ChunkPos(imd.getCenter());
                        this.machineData.put(chunk, imd);
                    });
            });
        }
    }

    @Override
    @Nonnull
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        if (!machineData.isEmpty()) {
            ListNBT collect = machineData.values()
                    .stream()
                    .map(data -> {
                        DataResult<INBT> n = CompactMachineInternalData.CODEC.encodeStart(NBTDynamicOps.INSTANCE, data);
                        return n.result();
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(NbtListCollector.toNbtList());

            nbt.put("machines", collect);
        }

        return nbt;
    }

    public boolean isRegistered(ChunkPos chunkPos) {
        return machineData.containsKey(chunkPos);
    }

    public void register(ChunkPos pos, CompactMachineInternalData data) throws OperationNotSupportedException {
        if(isRegistered(pos))
            throw new OperationNotSupportedException("Machine already registered.");

        machineData.put(pos, data);
        setDirty();
    }

    public Optional<CompactMachineInternalData> forChunk(ChunkPos chunkPos) {
        return Optional.ofNullable(machineData.get(chunkPos));
    }

    public int getNextId() {
        return this.machineData.size() + 1;
    }
}
