package com.robotgryphon.compactmachines.data.machines;

import com.robotgryphon.compactmachines.data.NbtListCollector;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds basic information about players inside a compact machine.
 */
public class CompactMachinePlayerData extends CompactMachineBaseData {

    private HashSet<UUID> internalPlayers;

    protected CompactMachinePlayerData() {
        super();
        this.internalPlayers = new HashSet<>(0);
    }

    public CompactMachinePlayerData(int id) {
        super(id);
        this.internalPlayers = new HashSet<>(0);
    }

    public static CompactMachinePlayerData fromNBT(INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            Optional<Integer> id = getIdFromNbt(nbt);

            // If we have an ID from nbt, create an instance, otherwise invalid
            return id.map(i -> {
                CompactMachinePlayerData d = new CompactMachinePlayerData(i);
                d.deserializeNBT((CompoundNBT) nbt);
                return d;
            }).orElse(null);
        }

        return null;
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        ListNBT ids = internalPlayers.stream()
                .map(u -> {
                    IntArrayNBT intNBTS = NBTUtil.func_240626_a_(u);
                    CompoundNBT pnbt = new CompoundNBT();
                    pnbt.put("id", intNBTS);
                    return pnbt;
                })
                .collect(NbtListCollector.toNbtList());
        nbt.put("players", ids);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        if(nbt.contains("players")) {
            ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
            players.forEach(playerData -> {
                CompoundNBT pd = (CompoundNBT) playerData;
                INBT playerId = pd.get("id");
                if(playerId != null) {
                    UUID id = NBTUtil.readUniqueId(playerId);
                    this.internalPlayers.add(id);
                }
            });
        }
    }

    public boolean hasPlayers() {
        return !internalPlayers.isEmpty();
    }

    public void addPlayer(ServerPlayerEntity serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().getId();
        internalPlayers.add(playerUUID);
    }

    public void removePlayer(ServerPlayerEntity serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().getId();
        if(internalPlayers.contains(playerUUID))
            internalPlayers.remove(playerUUID);
    }
}
