package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.SavedMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.network.MachinePlayersChangedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos, int machineId) {
        MinecraftServer serv = serverPlayer.getServer();
        SavedMachineData machineData = SavedMachineData.getInstance(serv);
        CompactMachineServerData serverData = machineData.getData();
        Optional<CompactMachinePlayerData> playerData = serverData.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.addPlayer(serverPlayer);
            machineData.setDirty();

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serv, machineId, serverPlayer.getUUID(), MachinePlayersChangedPacket.EnumPlayerChangeType.ENTERED);
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos, int machineId) {
        MinecraftServer serv = serverPlayer.getServer();
        SavedMachineData machineData = SavedMachineData.getInstance(serv);
        CompactMachineServerData serverData = machineData.getData();
        Optional<CompactMachinePlayerData> playerData = serverData.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.removePlayer(serverPlayer);
            machineData.setDirty();

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serv, machineId, serverPlayer.getUUID(), MachinePlayersChangedPacket.EnumPlayerChangeType.EXITED);
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }
}
