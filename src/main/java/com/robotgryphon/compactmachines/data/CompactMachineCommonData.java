package com.robotgryphon.compactmachines.data;

import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CompactMachineCommonData {
    protected Map<Integer, CompactMachinePlayerData> playerData;

    private static CompactMachineCommonData INSTANCE = new CompactMachineCommonData();

    protected CompactMachineCommonData() {
        this.playerData = new HashMap<>();
    }

    public static CompactMachineCommonData getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CompactMachineCommonData();
        }

        return INSTANCE;
    }

    public static CompoundNBT serializePlayerData(CompactMachineCommonData shared, CompoundNBT nbt) {
        ListNBT playerList = shared.playerData.values()
                .stream()
                .filter(CompactMachinePlayerData::hasPlayers)
                .map(CompactMachinePlayerData::serializeNBT)
                .collect(NbtListCollector.toNbtList());

        if (!playerList.isEmpty())
            nbt.put("players", playerList);

        return nbt;
    }

    public CompoundNBT serializeNBT(CompoundNBT compound) {
        compound = serializePlayerData(this, compound);
        return compound;
    }

    public void deserializeNBT(CompoundNBT nbt) {
        deserializePlayerData(nbt);
    }

    public void deserializePlayerData(CompoundNBT nbt) {
        if (nbt.contains("players")) {
            ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
            players.forEach(data -> {
                CompactMachinePlayerData pmd = CompactMachinePlayerData.fromNBT(data);
                playerData.put(pmd.getId(), pmd);
            });
        }
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

    public Optional<CompactMachinePlayerData> getPlayerData(int id) {
        if (!playerData.containsKey(id))
            playerData.put(id, new CompactMachinePlayerData(id));

        return Optional.ofNullable(playerData.get(id));
    }
}
