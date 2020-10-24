package com.robotgryphon.compactmachines.data;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;

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
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
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
