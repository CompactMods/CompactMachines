package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.data.CompactMachineMemoryData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.network.MachinePlayerAddedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, int machineId) {
        Optional<CompactMachinePlayerData> playerData = CompactMachineMemoryData.INSTANCE.getPlayerData(machineId);
        playerData.ifPresent(d -> {
            d.addPlayer(serverPlayer);

            RegistryKey<World> dimensionKey = serverPlayer.world.getDimensionKey();
            MachinePlayerAddedPacket p = new MachinePlayerAddedPacket(serverPlayer.getUniqueID());
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.DIMENSION.with(() -> dimensionKey),
                    p);
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
