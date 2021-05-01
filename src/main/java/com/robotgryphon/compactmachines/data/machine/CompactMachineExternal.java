package com.robotgryphon.compactmachines.data.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactmachines.data.codec.CodecExtensions;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.util.math.ChunkPos;

public class CompactMachineExternal {
    private final int machineId;
    public DimensionalPosition location;
    private ChunkPos chunk;

    public static final Codec<CompactMachineExternal> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("machine").forGetter(CompactMachineExternal::getMachineId),
            DimensionalPosition.CODEC.fieldOf("location").forGetter(CompactMachineExternal::getLocation),
            CodecExtensions.CHUNKPOS_CODEC.fieldOf("chunk").forGetter(CompactMachineExternal::getMappedChunk)
    ).apply(i, CompactMachineExternal::new));

    public CompactMachineExternal(int machineId, DimensionalPosition location, ChunkPos inside) {
        this.machineId = machineId;
        this.location = location;
        this.chunk = inside;
    }

    public int getMachineId() {
        return this.machineId;
    }

    public DimensionalPosition getLocation() {
        return this.location;
    }

    public ChunkPos getMappedChunk() {
        return this.chunk;
    }
}
