package com.robotgryphon.compactmachines.data.machines;

import com.robotgryphon.compactmachines.data.NbtListCollector;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.util.PlayerUtil;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.*;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds basic information about players inside a compact machine.
 */
public class CompactMachinePlayerData extends CompactMachineBaseData {

    private HashSet<UUID> internalPlayers;

    /**
     * Holds a mapping of where players entered a machine.
     */
    private HashMap<UUID, DimensionalPosition> externalSpawns;

    protected CompactMachinePlayerData() {
        super();
        this.internalPlayers = new HashSet<>(0);
        this.externalSpawns = new HashMap<>(0);
    }

    public CompactMachinePlayerData(int id) {
        super(id);
        this.internalPlayers = new HashSet<>(0);
        this.externalSpawns = new HashMap<>(0);
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
                    IntArrayNBT intNBTS = NBTUtil.createUUID(u);
                    CompoundNBT pnbt = new CompoundNBT();
                    pnbt.put("id", intNBTS);
                    return pnbt;
                })
                .collect(NbtListCollector.toNbtList());
        nbt.put("players", ids);


        ListNBT spawns = externalSpawns.entrySet()
                .stream()
                .map((pSpawn) -> {
                    CompoundNBT spawn = new CompoundNBT();

                    UUID playerId = pSpawn.getKey();
                    DimensionalPosition pSpawnPos = pSpawn.getValue();
                    spawn.put("id", NBTUtil.createUUID(playerId));
                    spawn.put("spawn", pSpawnPos.serializeNBT());

                    return spawn;
                })
                .collect(NbtListCollector.toNbtList());

        nbt.put("spawns", spawns);

        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);

        if (nbt.contains("players")) {
            ListNBT players = nbt.getList("players", Constants.NBT.TAG_COMPOUND);
            players.forEach(playerData -> {
                CompoundNBT pd = (CompoundNBT) playerData;
                UUID id = pd.getUUID("id");
                this.internalPlayers.add(id);
            });
        }

        if (nbt.contains("spawns")) {
            ListNBT spawns = nbt.getList("spawns", Constants.NBT.TAG_COMPOUND);
            spawns.forEach(spawnData -> {

                CompoundNBT spawn = (CompoundNBT) spawnData;
                UUID playerId = spawn.getUUID("id");
                DimensionalPosition pos = DimensionalPosition.fromNBT(spawn.getCompound("spawn"));

                externalSpawns.put(playerId, pos);
            });
        }
    }

    public boolean hasPlayers() {
        return !internalPlayers.isEmpty();
    }

    public void addPlayer(UUID playerID) {
        if(!internalPlayers.contains(playerID)) {
            internalPlayers.add(playerID);
        }
    }

    public void addPlayer(ServerPlayerEntity serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().getId();
        addPlayer(playerUUID);

        // server support - add spawn to data
        if(!externalSpawns.containsKey(playerUUID)) {
            DimensionalPosition pos = PlayerUtil.getPlayerDimensionalPosition(serverPlayer);
            externalSpawns.put(playerUUID, pos);
        }
    }

    public void removePlayer(UUID playerID) {
        if (internalPlayers.contains(playerID)) {
            internalPlayers.remove(playerID);
            externalSpawns.remove(playerID);
        }
    }

    public void removePlayer(ServerPlayerEntity serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().getId();
        removePlayer(playerUUID);
    }

    public Optional<DimensionalPosition> getExternalSpawn(ServerPlayerEntity serverPlayer) {
        UUID playerUUID = serverPlayer.getGameProfile().getId();
        if(!internalPlayers.contains(playerUUID))
            return Optional.empty();

        if(!externalSpawns.containsKey(playerUUID))
            return Optional.empty();

        return Optional.of(externalSpawns.get(playerUUID));
    }
}
