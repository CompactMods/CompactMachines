package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.data.CompactMachineMemoryData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.network.MachinePlayersChangedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, int machineId) {
        Optional<CompactMachinePlayerData> playerData = CompactMachineMemoryData.INSTANCE.getPlayerData(machineId);
        playerData.ifPresent(d -> {
            d.addPlayer(serverPlayer);
            CompactMachineMemoryData.markDirty(serverPlayer.getServer());

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serverPlayer.getUniqueID());
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> getPlayerChunk(serverPlayer)),
                    p);
        });
    }

    @Nonnull
    public static Chunk getPlayerChunk(ServerPlayerEntity serverPlayer) {
        return serverPlayer.getServerWorld().getChunkAt(serverPlayer.getPosition());
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, int machineId) {
        Optional<CompactMachinePlayerData> playerData = CompactMachineMemoryData.INSTANCE.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.removePlayer(serverPlayer);
            CompactMachineMemoryData.markDirty(serverPlayer.getServer());

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serverPlayer.getUniqueID());
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> getPlayerChunk(serverPlayer)),
                    p);
        });
    }
}
