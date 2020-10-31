package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.network.MachinePlayersChangedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, int machineId) {
        MinecraftServer serv = serverPlayer.getServer();
        CompactMachineServerData serverData = CompactMachineServerData.getInstance(serv);
        Optional<CompactMachinePlayerData> playerData = serverData.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.addPlayer(serverPlayer);
            serverData.markDirty();

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
        CompactMachineServerData serverData = CompactMachineServerData.getInstance(serverPlayer.getServer());
        Optional<CompactMachinePlayerData> playerData = serverData.getPlayerData(machineId);

        playerData.ifPresent(d -> {
            d.removePlayer(serverPlayer);
            serverData.markDirty();

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serverPlayer.getUniqueID());
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> getPlayerChunk(serverPlayer)),
                    p);
        });
    }
}
