package dev.compactmods.machines.upgrades.storage;

import dev.compactmods.machines.api.room.capability.RoomCapability;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.storage.tunnel.NamedStorageService;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.transfer.ResourceHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Room capabilities exposed by the storage system.
 *
 * <p>{@link #ROOM_STORAGE} gives a {@link RoomStorageCache} for a room, queried from a
 * {@link dev.compactmods.machines.api.room.RoomInstance RoomInstance} via
 * {@code room.getCapability(StorageCapabilities.ROOM_STORAGE)}.
 */
public final class StorageCapabilities {

    public static final RoomCapability<RoomStorageCache, Void> ROOM_STORAGE =
            RoomCapability.createVoid(CompactMachinesCore.identifier("room_storage"), RoomStorageCache.class);

    /** The room's named storages; see {@link NamedStorageService}. */
    public static final RoomCapability<NamedStorageService, Void> NAMED_STORAGES =
            RoomCapability.createVoid(CompactMachinesCore.identifier("named_storages"), NamedStorageService.class);

    // One cache instance per (server, room). The cache must persist between ticks to be useful, so it
    // cannot be rebuilt per capability query. Scoped by server via a weak key so entries drop when a
    // server is collected (e.g. integrated-server world reloads); room code keys are unique per server.
    private static final Map<MinecraftServer, Map<String, RoomStorageCache>> CACHES = new WeakHashMap<>();

    // Live named storage handlers per (server, room), by storage name. Same lifetime as CACHES: the
    // contents are memory only, so they last as long as the server does.
    private static final Map<MinecraftServer, Map<String, Map<String, ResourceHandler<?>>>> NAMED_HANDLERS = new WeakHashMap<>();

    private StorageCapabilities() {}

    /** Registers the {@link #ROOM_STORAGE} and {@link #NAMED_STORAGES} providers. Call during {@code RegisterCapabilitiesEvent}. */
    public static void registerProviders() {
        RoomCapability.register(ROOM_STORAGE, (server, room, _) ->
                CACHES.computeIfAbsent(server, s -> new HashMap<>())
                        .computeIfAbsent(room.code(), code -> new RoomStorageCache()));

        RoomCapability.register(NAMED_STORAGES, (server, room, _) -> new NamedStorageService(server, room,
                NAMED_HANDLERS.computeIfAbsent(server, s -> new HashMap<>())
                        .computeIfAbsent(room.code(), code -> new HashMap<>())));
    }
}
