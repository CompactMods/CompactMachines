package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.MachineData;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, int machineId) {
        ServerWorld compactWorld = serverPlayer.getServer().getWorld(Registrations.COMPACT_DIMENSION);
        Optional<MachineData> mdo = CompactMachineUtil.getMachineData(compactWorld);

        mdo.ifPresent(machData -> {
            Optional<CompactMachinePlayerData> playerData = getOrCreatePlayerData(machineId, machData);

            playerData.ifPresent(d -> {
                d.addPlayer(serverPlayer);
                machData.updatePlayerData(d);
            });
        });
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, int machineId) {
        ServerWorld compactWorld = serverPlayer.getServer().getWorld(Registrations.COMPACT_DIMENSION);
        Optional<MachineData> mdo = CompactMachineUtil.getMachineData(compactWorld);
        mdo.ifPresent(machData -> {
            Optional<CompactMachinePlayerData> playerData = getOrCreatePlayerData(machineId, machData);

            playerData.ifPresent(d -> {
                d.removePlayer(serverPlayer);
                machData.updatePlayerData(d);
            });
        });
    }

    @Nonnull
    public static Optional<CompactMachinePlayerData> getOrCreatePlayerData(int machineId, MachineData machData) {
        Optional<CompactMachinePlayerData> playerData = machData.getPlayerData(machineId);
        if (!playerData.isPresent()) {
            // Something went wrong with datagen, try to recreate data
            CompactMachinePlayerData pd = new CompactMachinePlayerData(machineId);
            machData.updatePlayerData(pd);
            playerData = Optional.of(pd);
        }
        return playerData;
    }
}
