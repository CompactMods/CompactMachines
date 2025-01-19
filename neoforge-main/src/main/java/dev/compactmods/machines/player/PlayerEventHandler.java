package dev.compactmods.machines.player;

import dev.compactmods.machines.network.room.SyncRoomMetadataPacket;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerEventHandler {

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onPlayerJoinedServer);
    }

    public static void onPlayerJoinedServer(PlayerEvent.PlayerLoggedInEvent event) {
        final var player = event.getEntity();

        if(player instanceof ServerPlayer serverPlayer) {
            final var currentRoom = serverPlayer.getExistingData(Rooms.DataAttachments.CURRENT_ROOM_CODE);
            currentRoom.ifPresent(roomCode -> PacketDistributor.sendToPlayer(serverPlayer, new SyncRoomMetadataPacket(roomCode, Util.NIL_UUID)));
        }
    }
}
