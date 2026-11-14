package dev.compactmods.machines.upgrades.storage;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Per-room cache of discovered resource storage positions, so that the expensive volume scan does not
 * run every tick.
 *
 * <p>Only the <em>set of positions</em> is cached and refreshed on a fixed tick interval — the live
 * handlers are re-resolved on every {@link #resources} call. That split is deliberate: scanning a
 * whole room's volume is the costly operation worth caching, while re-resolving a handful of known
 * positions is cheap and keeps the returned handlers from ever going stale (a block removed between
 * scans simply resolves to {@code null} and is dropped; one added is picked up at the next scan).
 *
 * <p>The cache is keyed per {@link ResourceStorageType}, each with its own refresh clock. It is not
 * thread-safe; it is meant to be used from the server thread via {@link StorageCapabilities#ROOM_STORAGE}.
 */
public final class RoomStorageCache {

    /** Default refresh interval, in ticks (5 seconds). */
    public static final int DEFAULT_REFRESH_TICKS = 100;

    private final int refreshIntervalTicks;
    private final Map<ResourceStorageType<?>, TypeCache> byType = new HashMap<>();

    public RoomStorageCache(int refreshIntervalTicks) {
        this.refreshIntervalTicks = refreshIntervalTicks;
    }

    public RoomStorageCache() {
        this(DEFAULT_REFRESH_TICKS);
    }

    /**
     * {@return the storages of {@code type} in {@code room}, resolved to live handlers} The positions
     * are rescanned only once per refresh interval; handlers are resolved fresh on each call.
     */
    public <T extends Resource> Stream<LocatedResourceStorage<T>> resources(RoomInstance room, ResourceStorageType<T> type) {
        final var level = room.level();
        final var server = level.getServer();
        final var now = level.getGameTime();

        final var cache = byType.computeIfAbsent(type, t -> new TypeCache());
        if (now >= cache.nextScanTick) {
            cache.positions = scan(room, type);
            cache.nextScanTick = now + refreshIntervalTicks;
        }

        return cache.positions.stream()
                .map(pos -> resolve(server, type, pos))
                .filter(Objects::nonNull);
    }

    /** Forces the next {@link #resources} call to rescan every type, e.g. after a known room change. */
    public void invalidate() {
        byType.clear();
    }

    private static <T extends Resource> @Nullable LocatedResourceStorage<T> resolve(
            MinecraftServer server, ResourceStorageType<T> type, ResourceStoragePosition<?> position) {

        // Safe: the cache list is keyed by, and only ever holds positions of, this exact type.
        @SuppressWarnings("unchecked")
        final var typed = (ResourceStoragePosition<T>) position;

        final var handler = typed.resolve(server);
        return handler == null ? null : new LocatedResourceStorage<>(typed, handler);
    }

    private static <T extends Resource> List<ResourceStoragePosition<?>> scan(RoomInstance room, ResourceStorageType<T> type) {
        return ResourceStorageScanner.forResource(type)
                .filterPositions(room.boundaries().innerBounds())
                .scan(room.level())
                .map(LocatedResourceStorage::storage)
                .collect(Collectors.toList());
    }

    private static final class TypeCache {
        private long nextScanTick = Long.MIN_VALUE;
        private List<ResourceStoragePosition<?>> positions = List.of();
    }
}
