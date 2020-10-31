package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.machines.CompactMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CompactMachineMemoryData implements INBTSerializable<CompoundNBT> {
    private Map<Integer, CompactMachineData> machineData;
    private Map<Integer, CompactMachinePlayerData> playerData;

    public static CompactMachineMemoryData INSTANCE = new CompactMachineMemoryData();

    private CompactMachineMemoryData() {
        this.machineData = new HashMap<>();
        this.playerData = new HashMap<>();
    }

    public static void markDirty(MinecraftServer server) {
        ServerWorld compactWorld = server.getWorld(Registrations.COMPACT_DIMENSION);
        MachineData machineData = MachineData.getMachineData(compactWorld);
        machineData.markDirty();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compound = new CompoundNBT();
        ListNBT machineList = machineData.values()
                .stream()
                .map(CompactMachineData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        compound.put("machines", machineList);

        ListNBT playerList = playerData.values()
                .stream()
                .filter(CompactMachinePlayerData::hasPlayers)
                .map(CompactMachinePlayerData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        if(!playerList.isEmpty())
            compound.put("players", playerList);

        return compound;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("machines")) {
            ListNBT machines = nbt.getList("machines", Constants.NBT.TAG_COMPOUND);
            machines.forEach(data -> {
                CompactMachineData md = CompactMachineData.fromNBT(data);
                machineData.put(md.getId(), md);
            });
        }

        if (nbt.contains("players")) {
            ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
            players.forEach(data -> {
                CompactMachinePlayerData pmd = CompactMachinePlayerData.fromNBT(data);
                playerData.put(pmd.getId(), pmd);
            });
        }
    }

    public static int getNextMachineId(ServerWorld world) {
        return INSTANCE.machineData.size() + 1;
    }

    public boolean registerMachine(int newID, CompactMachineData compactMachineData) {
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

    public Stream<CompactMachineData> getMachines() {
        return machineData.values().stream();
    }

    public Optional<CompactMachineData> getMachineContainingPosition(Vector3d position) {
        return getMachines()
                .filter(machine -> {
                    BlockPos center = machine.getCenter();
                    AxisAlignedBB bounds = new AxisAlignedBB(center, center)
                            .grow(machine.getSize().getInternalSize());

                    return bounds.contains(position);
                })
                .findFirst();
    }

    public Optional<CompactMachineData> getMachineContainingPosition(BlockPos position) {
        AxisAlignedBB possibleCenters = new AxisAlignedBB(position, position).grow(EnumMachineSize.maximum().getInternalSize());

        return getMachines()
                .filter(machine -> {
                    BlockPos center = machine.getCenter();
                    Vector3d center3d = new Vector3d(center.getX(), center.getY(), center.getZ());
                    return possibleCenters.contains(center3d);
                })
                .findFirst();
    }

    public void updateMachineData(CompactMachineData d) {
        int id = d.getId();
        if (!machineData.containsKey(id))
            return;

        machineData.replace(id, d);
    }

    public void updatePlayerData(CompactMachinePlayerData pd) {
        int id = pd.getId();

        // Do we have an existing player data entry? If not, just add and return
        if (!playerData.containsKey(id)) {
            playerData.put(id, pd);
            return;
        }

        // If we have an existing entry, update and mark dirty
        playerData.replace(id, pd);
    }

    public Optional<CompactMachineData> getMachineData(int machineId) {
        if (!machineData.containsKey(machineId))
            return Optional.empty();

        return Optional.ofNullable(machineData.get(machineId));
    }

    public Optional<CompactMachinePlayerData> getPlayerData(int id) {
        if (!playerData.containsKey(id))
            playerData.put(id, new CompactMachinePlayerData(id));

        return Optional.ofNullable(playerData.get(id));
    }
}
