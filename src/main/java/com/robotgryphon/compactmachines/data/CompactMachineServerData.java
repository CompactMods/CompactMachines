package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CompactMachineServerData extends CompactMachineCommonData {

    private Map<Integer, CompactMachineRegistrationData> machineData;

    @Nullable
    private MinecraftServer server;

    private CompactMachineServerData() {
        super();
        this.machineData = new HashMap<>();
    }

    private CompactMachineServerData(MinecraftServer server) {
        this();
        this.server = server;
    }

    public static CompactMachineServerData getInstance(MinecraftServer server) {
        CompactMachineServerData serverData = SavedMachineData
                .getMachineData(server)
                .getServerData();

        serverData.server = server;
        return serverData;
    }

    public static CompactMachineServerData fromNbt(CompoundNBT nbt) {
        CompactMachineServerData d = new CompactMachineServerData();
        d.deserializeNBT(nbt);
        return d;
    }

    @Override
    public CompoundNBT serializeNBT(CompoundNBT nbt) {
        nbt = super.serializeNBT(nbt);

        ListNBT machineList = machineData.values()
                .stream()
                .map(CompactMachineRegistrationData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        nbt.put("machines", machineList);

        return nbt;
    }


    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        if (nbt.contains("machines")) {
            ListNBT machines = nbt.getList("machines", Constants.NBT.TAG_COMPOUND);
            machines.forEach(data -> {
                CompactMachineRegistrationData md = CompactMachineRegistrationData.fromNBT(data);
                machineData.put(md.getId(), md);
            });
        }
    }

    @Override
    public void markDirty() {
        SavedMachineData machineData = SavedMachineData.getMachineData(server);
        machineData.markDirty();
    }

    public int getNextMachineId() {
        return machineData.size() + 1;
    }

    public boolean registerMachine(int newID, CompactMachineRegistrationData compactMachineData) {
        if (machineData.containsKey(newID))
            return false;

        this.machineData.put(newID, compactMachineData);
        this.playerData.put(newID, new CompactMachinePlayerData(newID));
        return true;
    }


    public Stream<AxisAlignedBB> getAllMachineBounds() {
        return machineData.values().stream()
                .map(mach -> new AxisAlignedBB(mach.getCenter(), mach.getCenter())
                        .grow(mach.getSize().getInternalSize()));
    }

    public Stream<CompactMachineRegistrationData> getMachines() {
        return machineData.values().stream();
    }

    public Optional<CompactMachineRegistrationData> getMachineContainingPosition(Vector3d position) {
        return getMachines()
                .filter(machine -> {
                    BlockPos center = machine.getCenter();
                    AxisAlignedBB bounds = new AxisAlignedBB(center, center)
                            .grow(machine.getSize().getInternalSize());

                    return bounds.contains(position);
                })
                .findFirst();
    }

    public Optional<CompactMachineRegistrationData> getMachineContainingPosition(BlockPos position) {
        AxisAlignedBB possibleCenters = new AxisAlignedBB(position, position).grow(EnumMachineSize.maximum().getInternalSize());

        return getMachines()
                .filter(machine -> {
                    BlockPos center = machine.getCenter();
                    Vector3d center3d = new Vector3d(center.getX(), center.getY(), center.getZ());
                    return possibleCenters.contains(center3d);
                })
                .findFirst();
    }

    public void updateMachineData(CompactMachineRegistrationData d) {
        int id = d.getId();
        if (!machineData.containsKey(id))
            return;

        machineData.replace(id, d);
        markDirty();
    }

    public Optional<CompactMachineRegistrationData> getMachineData(int machineId) {
        if (!machineData.containsKey(machineId))
            return Optional.empty();

        return Optional.ofNullable(machineData.get(machineId));
    }
}
