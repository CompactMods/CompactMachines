package com.robotgryphon.compactmachines.data.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactmachines.data.codec.CodecExtensions;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CompactMachineInternalData {

    public static final Codec<CompactMachineInternalData> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.UUID_CODEC.fieldOf("owner").forGetter(CompactMachineInternalData::getOwner),
            BlockPos.CODEC.fieldOf("center").forGetter(CompactMachineInternalData::getCenter),
            BlockPos.CODEC.fieldOf("spawn").forGetter(CompactMachineInternalData::getSpawn),
            EnumMachineSize.CODEC.fieldOf("size").forGetter(CompactMachineInternalData::getSize)
    ).apply(i, CompactMachineInternalData::new));

    private final UUID owner;
    private final BlockPos center;
    private BlockPos spawn;
    private final EnumMachineSize size;

    public CompactMachineInternalData(UUID owner, BlockPos center, BlockPos spawn, EnumMachineSize size) {
        this.owner = owner;
        this.center = center;
        this.spawn = spawn;
        this.size = size;
    }

    private EnumMachineSize getSize() {
        return this.size;
    }

    public UUID getOwner() { return this.owner; }

    public BlockPos getSpawn() {
        if(this.spawn != null)
            return this.spawn;

        BlockPos.Mutable newSpawn = center.mutable();
        newSpawn.setY(newSpawn.getY() - (size.getInternalSize() / 2));

        this.spawn = newSpawn;
        return this.spawn;
    }

    public BlockPos getCenter() {
        return this.center;
    }

    public void setSpawn(BlockPos newSpawn) {
        this.spawn = newSpawn;
    }

    public AxisAlignedBB getMachineBounds() {
        return size.getBounds(this.center);
    }
}
