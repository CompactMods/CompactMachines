package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import dev.compactmods.machines.room.network.RoomEntitiesPacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side store of the latest live entities per tracked room (room-local space), delivered by
 * {@link RoomEntitiesPacket}. The preview entity pass reads
 * this each frame to place and draw the entities inside the room preview.
 *
 * <p>Updated on the client thread; kept concurrent because the preview renderer reads it off the main
 * thread. Each packet replaces the room's list wholesale.
 */
public final class ClientRoomEntities {

    private static final Map<String, List<RoomEntitySnapshot>> ENTITIES = new ConcurrentHashMap<>();

    private ClientRoomEntities() {}

    /** Replaces the stored entities for {@code roomCode} and applies the update to the render pool. */
    public static void accept(String roomCode, List<RoomEntitySnapshot> entities) {
        if (entities.isEmpty()) {
            ENTITIES.remove(roomCode);
            return;
        }
        ENTITIES.put(roomCode, List.copyOf(entities));
        for (RoomEntitySnapshot snapshot : entities)
            RoomPreviewEntities.update(roomCode, snapshot);
    }

    /** {@return the latest entities for {@code roomCode}, or an empty list} */
    public static List<RoomEntitySnapshot> get(String roomCode) {
        return ENTITIES.getOrDefault(roomCode, List.of());
    }

    /** Forgets {@code roomCode}'s entities (its render instances drop on the next prune). */
    public static void remove(String roomCode) {
        ENTITIES.remove(roomCode);
    }

    public static void clear() {
        ENTITIES.clear();
    }
}
