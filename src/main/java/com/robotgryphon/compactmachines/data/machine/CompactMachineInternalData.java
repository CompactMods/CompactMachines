package com.robotgryphon.compactmachines.data.machine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class CompactMachineInternalData {

    public static Codec<CompactMachineInternalData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("owner").forGetter(CompactMachineInternalData::getOwnerString),
            BlockPos.CODEC.fieldOf("center").forGetter(CompactMachineInternalData::getCenter),
            BlockPos.CODEC.fieldOf("spawn").forGetter(CompactMachineInternalData::getSpawn),
            Codec.STRING.fieldOf("size").forGetter(CompactMachineInternalData::getSizeString)
    ).apply(i, CompactMachineInternalData::new));

    private final UUID owner;
    private final BlockPos center;
    private BlockPos spawn;
    private final EnumMachineSize size;

    protected CompactMachineInternalData(String owner, BlockPos center, BlockPos spawn, String sizeString) {
        this.owner = UUID.fromString(owner);
        this.center = center;
        this.spawn = spawn;
        this.size = EnumMachineSize.getFromSize(sizeString);
    }

    public CompactMachineInternalData(UUID owner, BlockPos center, BlockPos spawn, EnumMachineSize size) {
        this.owner = owner;
        this.center = center;
        this.spawn = spawn;
        this.size = size;
    }

    private String getSizeString() {
        return this.size.name();
    }

    public UUID getOwner() { return this.owner; }

    private String getOwnerString() {
        return owner.toString();
    }

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
