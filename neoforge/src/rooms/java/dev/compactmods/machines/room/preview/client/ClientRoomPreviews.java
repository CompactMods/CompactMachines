package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side store of the latest {@link RoomPreviewSnapshot} received per room code.
 *
 * <p>Snapshots arrive on the client thread (via {@link #accept}) but are read by the preview mesher
 * off the render thread, so the backing map is concurrent and each entry carries a monotonically
 * increasing {@link Entry#version() version}. A mesher caches meshes keyed by {@code (roomCode,
 * version)} and rebuilds only when the version advances — that's what lets the (heavier) mesh build
 * run "every second or so" independent of how often snapshots land.
 */
public final class ClientRoomPreviews {

    private static final Map<String, Entry> PREVIEWS = new ConcurrentHashMap<>();

    /** Rooms an open GUI wants previews for, on top of the machines visible in the world. */
    private static final Set<String> WATCHED = ConcurrentHashMap.newKeySet();

    /** This player's spawn in a room: room-local feet position, and the yaw/pitch (degrees) faced on arrival. */
    public record Spawn(Vector3fc position, float yaw, float pitch) {}

    /** This player's spawn per room (see {@code RoomPreviewSpawnPacket}). */
    private static final Map<String, Spawn> SPAWNS = new ConcurrentHashMap<>();

    /**
     * Block entity data newer than each room's snapshot (see {@code RoomBlockEntitiesPacket}), by
     * room-local position. Replaced wholesale by a new snapshot, which is newer than all of it.
     */
    private static final Map<String, Map<BlockPos, RoomBlockEntitySnapshot>> BLOCK_ENTITIES = new ConcurrentHashMap<>();

    private ClientRoomPreviews() {}

    /** A stored snapshot plus a version that increments each time the room's snapshot is replaced. */
    public record Entry(RoomPreviewSnapshot snapshot, int version) {}

    /** Stores the newest snapshot for {@code roomCode}, bumping its version. Called on the client thread. */
    public static void accept(String roomCode, RoomPreviewSnapshot snapshot) {
        PREVIEWS.compute(roomCode, (code, previous) ->
                new Entry(snapshot, previous == null ? 1 : previous.version() + 1));
        BLOCK_ENTITIES.remove(roomCode);
        dev.compactmods.machines.core.CompactMachinesCore.modLog().info("[RoomPreview] client received snapshot {}: {}x{}x{} palette={} empty={}",
                roomCode, snapshot.sizeX(), snapshot.sizeY(), snapshot.sizeZ(), snapshot.palette().size(), snapshot.isEmpty());
    }

    /** {@return the latest stored preview for {@code roomCode}, or {@code null} if none has arrived} */
    public static @Nullable Entry get(String roomCode) {
        return PREVIEWS.get(roomCode);
    }

    /** Stores block entity data newer than the room's snapshot and applies it to the room's preview. Client thread. */
    public static void acceptBlockEntities(String roomCode, List<RoomBlockEntitySnapshot> changed) {
        final var latest = BLOCK_ENTITIES.computeIfAbsent(roomCode, c -> new ConcurrentHashMap<>());
        for (RoomBlockEntitySnapshot be : changed)
            latest.put(be.local(), be);
        RoomPreviewMeshCache.acceptBlockEntities(roomCode, changed);
    }

    /** {@return block entity data newer than the room's snapshot}, by room-local position */
    public static Map<BlockPos, RoomBlockEntitySnapshot> latestBlockEntities(String roomCode) {
        return BLOCK_ENTITIES.getOrDefault(roomCode, Map.of());
    }

    public static boolean has(String roomCode) {
        return PREVIEWS.containsKey(roomCode);
    }

    public static void acceptSpawn(String roomCode, Spawn spawn) {
        SPAWNS.put(roomCode, new Spawn(new Vector3f(spawn.position()), spawn.yaw(), spawn.pitch()));
    }

    /** {@return this player's spawn in {@code roomCode}}, or {@code null} if not received */
    public static @Nullable Spawn spawn(String roomCode) {
        return SPAWNS.get(roomCode);
    }

    /** Keep receiving snapshots for {@code roomCode} until {@link #unwatch}, even if no machine for it is visible. */
    public static void watch(String roomCode) {
        WATCHED.add(roomCode);
    }

    public static void unwatch(String roomCode) {
        WATCHED.remove(roomCode);
    }

    /** {@return a copy of the rooms explicitly watched by open GUIs} */
    public static Set<String> watched() {
        return Set.copyOf(WATCHED);
    }

    /** Drops all stored previews and overlay data — call on disconnect so a new session starts clean. */
    public static void clear() {
        PREVIEWS.clear();
        WATCHED.clear();
        SPAWNS.clear();
        BLOCK_ENTITIES.clear();
        RoomPreviewOverlay.all().forEach(RoomPreviewOverlay::clear);
    }
}
