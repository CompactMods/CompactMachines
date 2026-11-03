package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caches the baked {@link RoomPreviewMesh} per room so its (textured) geometry is rebuilt only when
 * that room's snapshot changes (tracked by {@link ClientRoomPreviews.Entry#version()}). Because each
 * mesh owns GPU buffers, superseding or clearing an entry frees the old one.
 *
 * <p>Accessed only from the render thread (block-entity render-state extraction), so it needs no
 * synchronisation; it reads snapshots from the concurrent {@link ClientRoomPreviews} store.
 */
public final class RoomPreviewMeshCache {

    private static final Map<String, Cached> CACHE = new HashMap<>();

    private RoomPreviewMeshCache() {}

    private record Cached(int version, RoomPreviewMesh mesh) {}

    /** A version no snapshot has: the mesh is rebaked on next use. */
    private static final int STALE = -1;

    /**
     * {@return the mesh for {@code roomCode}, baking it if the stored snapshot is newer than the
     * cached mesh, or {@code null} if there is no snapshot / nothing to draw}
     */
    public static @Nullable RoomPreviewMesh get(String roomCode) {
        final ClientRoomPreviews.Entry entry = ClientRoomPreviews.get(roomCode);
        if (entry == null) {
            evict(roomCode);
            return null;
        }

        final Cached cached = CACHE.get(roomCode);
        if (cached != null && cached.version == entry.version())
            return cached.mesh.isEmpty() ? null : cached.mesh;

        // Keep the room's block entities across versions (and remeshes), so they carry on animating
        final PreviewLevel previous = cached != null ? cached.mesh.level() : null;
        evict(roomCode); // free the stale mesh's GPU buffers before rebaking
        final RoomPreviewMesh mesh = RoomPreviewMesh.build(entry.snapshot(), ClientRoomPreviews.latestBlockEntities(roomCode), previous);
        CACHE.put(roomCode, new Cached(entry.version(), mesh));
        return mesh.isEmpty() ? null : mesh;
    }

    /**
     * Applies newer block entity data to the room's preview in place. If it changed what a block's model
     * looks like (a pipe connecting, say), the mesh is rebaked on next use, keeping the block entities.
     */
    static void acceptBlockEntities(String roomCode, List<RoomBlockEntitySnapshot> changed) {
        final Cached cached = CACHE.get(roomCode);
        if (cached == null || cached.mesh.level() == null)
            return; // Kept in ClientRoomPreviews; the next bake starts from it

        if (cached.mesh.level().applyBlockEntities(changed))
            CACHE.put(roomCode, new Cached(STALE, cached.mesh));
    }

    /** Ticks the block entities of every cached room's preview. Client thread, once per client tick. */
    public static void tickAll() {
        for (Cached cached : CACHE.values()) {
            if (cached.mesh.level() != null)
                cached.mesh.level().tick();
        }
    }

    private static void evict(String roomCode) {
        final Cached old = CACHE.remove(roomCode);
        if (old != null) old.mesh.close();
    }

    /** Frees and drops all cached meshes — call on disconnect alongside {@link ClientRoomPreviews#clear()}. */
    public static void clear() {
        for (Cached cached : CACHE.values())
            cached.mesh.close();
        CACHE.clear();
    }
}
