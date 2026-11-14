package dev.compactmods.machines.room.preview.server;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Extra per-room data for room previews, from systems the preview doesn't know about (tunnels, for
 * example). {@link RoomPreviewService} captures it alongside each block scan and when a player starts
 * watching a room, and sends it to the room's watchers only when it changed ({@code equals}).
 *
 * <p>Register with {@link RoomPreviewService#addContributor}. Runs on the server thread.
 */
@FunctionalInterface
public interface RoomPreviewContributor {

    /** {@return this contributor's payload for {@code room}}, or empty to send nothing */
    Optional<CustomPacketPayload> capture(ServerLevel level, RoomInstance room);
}
