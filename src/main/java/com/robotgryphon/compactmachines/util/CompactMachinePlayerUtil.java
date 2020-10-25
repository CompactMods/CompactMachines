package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.data.CompactMachineMemoryData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, int machineId) {
        Optional<CompactMachinePlayerData> playerData = CompactMachineMemoryData.INSTANCE.getPlayerData(machineId);
        playerData.ifPresent(d -> {
            d.addPlayer(serverPlayer);

            // TODO send network packet
        });
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, int machineId) {
        Optional<CompactMachinePlayerData> playerData = CompactMachineMemoryData.INSTANCE.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.removePlayer(serverPlayer);

            // TODO send network packet
        });
    }
}
