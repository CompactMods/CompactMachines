package dev.compactmods.machines.player;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomDebugInformation;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.Logger;

public class PlayerEventHandler {
    private static final Logger MOD_LOG = LoggingUtil.modLog();

    public static void registerEvents() {
        NeoForge.EVENT_BUS.addListener(PlayerEventHandler::onPlayerJoinedServer);
    }

    public static void onPlayerJoinedServer(PlayerEvent.PlayerLoggedInEvent event) {
        final var player = event.getEntity();

        if(player instanceof ServerPlayer serverPlayer && CompactDimension.isInServerDimension(serverPlayer)) {
            String currentRoom = CompactMachines.chunkManager()
                    .findRoomByChunk(serverPlayer.chunkPosition())
                    .orElse(null);

            MOD_LOG.debug("Player joined server ({}); they are supposedly in the compact dimension. Room Code: {}",
                    serverPlayer.getUUID(), currentRoom);

            if(currentRoom == null) {
                // Kick player out of space
                MOD_LOG.warn("Room not found. Either data got corrupted or the player escaped a room. Checking...");

                PlayerUtil.handlePlayerMaybeEscaped(serverPlayer, serverPlayer.server.getGameRules());
                return;
            }

            final var room = CompactMachines.room(serverPlayer.server, currentRoom)
                            .orElseThrow();

            final var owner = room.getData(CMDataAttachments.ROOM_OWNER);

            final var isInRoomBounds = room.boundaries()
                    .innerBounds()
                    .intersects(player.getBoundingBox());

            if(!isInRoomBounds) {
                // Kick player out of space
                MOD_LOG.warn("Room found. But it looks like the player escaped the room boundaries. Checking...");

                PlayerUtil.handlePlayerMaybeEscaped(serverPlayer, serverPlayer.server.getGameRules());
                return;
            }

            serverPlayer.setData(CMDataAttachments.CURRENT_ROOM_CODE, room.code());
            serverPlayer.setData(CMDataAttachments.CURRENT_ROOM_DEBUG_INFO, new RoomDebugInformation(room.code(), owner));
        }
    }
}
