package com.robotgryphon.compactmachines.util;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.data.legacy.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.legacy.SavedMachineData;
import com.robotgryphon.compactmachines.data.player.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.network.MachinePlayersChangedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Optional;

public class CompactMachinePlayerUtil {
    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos) {
        MinecraftServer serv = serverPlayer.getServer();
        if (serv == null)
            return;

        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        CompactMachineTile tile = (CompactMachineTile) serverPlayer.getLevel().getBlockEntity(machinePos);
        if(tile == null)
            return;

        tile.getInternalChunkPos().ifPresent(mChunk -> {

            playerData.addPlayer(serverPlayer, mChunk);
            playerData.setDirty();

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serv, tile.machineId, serverPlayer.getUUID(), MachinePlayersChangedPacket.EnumPlayerChangeType.ENTERED);
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos) {
        MinecraftServer serv = serverPlayer.getServer();

        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        playerData.removePlayer(serverPlayer);

        CompactMachineTile tile = (CompactMachineTile) serverPlayer.getLevel().getBlockEntity(machinePos);
        if(tile == null)
            return;

        tile.getInternalChunkPos().ifPresent(mChunk -> {

            playerData.removePlayer(serverPlayer);
            playerData.setDirty();

            MachinePlayersChangedPacket p = new MachinePlayersChangedPacket(serv, tile.machineId, serverPlayer.getUUID(), MachinePlayersChangedPacket.EnumPlayerChangeType.EXITED);
            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }
}
