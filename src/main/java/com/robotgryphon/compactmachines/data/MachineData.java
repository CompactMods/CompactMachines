package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MachineData extends WorldSavedData {

    public final static String DATA_NAME = CompactMachines.MODID + "_machines";

    private Map<Integer, CompactMachineData> machineData;
    private Map<Integer, CompactMachinePlayerData> playerData;

    public MachineData() {
        super(DATA_NAME);
        this.machineData = new HashMap<>();
        this.playerData = new HashMap<>();
    }

    @Nonnull
    public static MachineData getMachineData(ServerWorld world) {
        DimensionSavedDataManager sd = world.getSavedData();
        return sd.getOrCreate(MachineData::new, DATA_NAME);
    }

    @Override
    public void read(CompoundNBT nbt) {
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

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT machineList = machineData.values()
                .stream()
                .map(CompactMachineData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        compound.put("machines", machineList);

        ListNBT playerList = playerData.values()
                .stream()
                .map(CompactMachinePlayerData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        compound.put("players", playerList);

        return compound;
    }

    public static int getNextMachineId(ServerWorld world) {
        MachineData machineData = getMachineData(world);
        if (machineData.machineData == null)
            return 0;

        return machineData.machineData.size() + 1;
    }

    public boolean registerMachine(int newID, CompactMachineData compactMachineData) {
        if (machineData.containsKey(newID))
            return false;

        this.machineData.put(newID, compactMachineData);
        this.playerData.put(newID, new CompactMachinePlayerData(newID));
        this.markDirty();
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
        this.markDirty();
    }

    public void updatePlayerData(CompactMachinePlayerData pd) {
        int id = pd.getId();

        // Do we have a registered machine with that ID?
        if (!machineData.containsKey(id)) {
            CompactMachines.LOGGER.error("Tried to set player data on machine that does not have information registered.");
            return;
        }

        // Do we have an existing player data entry? If not, just add and return
        if (!playerData.containsKey(id)) {
            playerData.put(id, pd);
            this.markDirty();
            return;
        }

        // If we have an existing entry, update and mark dirty
        playerData.replace(id, pd);
        this.markDirty();
    }

    public Optional<CompactMachineData> getMachineData(int machineId) {
        if (!machineData.containsKey(machineId))
            return Optional.empty();

        return Optional.ofNullable(machineData.get(machineId));
    }

    public Optional<CompactMachinePlayerData> getPlayerData(int id) {
        if (!playerData.containsKey(id))
            return Optional.empty();

        return Optional.ofNullable(playerData.get(id));
    }
}
