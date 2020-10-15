package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.IntArrayNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.Optional;
import java.util.UUID;

/**
 * Holds information that can be used to uniquely identify a compact machine.
 */
public class CompactMachineData implements INBTSerializable<CompoundNBT> {

    private int id;
    private BlockPos center;
    private BlockPos spawnPoint;
    private UUID owner;
    private EnumMachineSize size;

    private CompactMachineData() {
    }

    public CompactMachineData(int id, BlockPos center, UUID owner, EnumMachineSize size) {
        this.id = id;
        this.center = center;
        this.owner = owner;
        this.size = size;
    }

    public static CompactMachineData fromNBT(INBT machine) {
        if (machine instanceof CompoundNBT) {
            CompactMachineData d = new CompactMachineData();
            d.deserializeNBT((CompoundNBT) machine);

            return d;
        }

        return null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();

        nbt.putInt("id", this.id);

        CompoundNBT centerNbt = NBTUtil.writeBlockPos(this.center);
        nbt.put("center", centerNbt);

        if(this.spawnPoint != null) {
            CompoundNBT spawnNbt = NBTUtil.writeBlockPos(this.spawnPoint);
            nbt.put("spawn", spawnNbt);
        }

        IntArrayNBT ownerNbt = NBTUtil.func_240626_a_(this.owner);
        nbt.put("owner", ownerNbt);

        nbt.putString("size", size.getName());

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("id")) {
            this.id = nbt.getInt("id");
        }

        if (nbt.contains("size")) {
            this.size = EnumMachineSize.getFromSize(nbt.getString("size"));
        }

        if (nbt.contains("owner")) {
            INBT ownerNbt = nbt.get("owner");
            if (ownerNbt != null)
                this.owner = NBTUtil.readUniqueId(ownerNbt);
        }

        if (nbt.contains("center")) {
            this.center = NBTUtil.readBlockPos(nbt.getCompound("center"));
        }

        if(nbt.contains("spawn")) {
            this.spawnPoint = NBTUtil.readBlockPos(nbt.getCompound("spawn"));
        }
    }

    public BlockPos getCenter() {
        return center;
    }

    public Optional<BlockPos> getSpawnPoint() {
        return Optional.ofNullable(this.spawnPoint);
    }

    public UUID getOwner() {
        return owner;
    }

    public EnumMachineSize getSize() {
        return size;
    }

    public int getId() {
        return id;
    }

    public void setSpawnPoint(BlockPos position) {
        this.spawnPoint = position;
    }
}
