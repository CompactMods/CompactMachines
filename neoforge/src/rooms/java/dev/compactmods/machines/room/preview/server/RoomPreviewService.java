package dev.compactmods.machines.room.preview.server;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.network.RoomEntitiesPacket;
import dev.compactmods.machines.room.network.RoomPreviewSnapshotPacket;
import dev.compactmods.machines.room.network.RoomPreviewSpawnPacket;
import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Server-side driver for room previews: tracks which players want which rooms, periodically captures
 * the interior of every <em>subscribed and loaded</em> room, and pushes {@link RoomPreviewSnapshot}s
 * to the interested clients only when a room's contents actually change.
 *
 * <p>All work runs on the server thread — {@link #onLevelTick} fires on the compact dimension's tick,
 * which is where the room interiors live, and the block reads must not run off-thread. The scan is
 * throttled to {@link #GATHER_INTERVAL_TICKS} and further limited to rooms someone is subscribed to,
 * so an idle server does no work. The (heavier) job of turning a snapshot into a mesh happens on the
 * client, off the render thread.
 *
 * <p>State is kept per {@link MinecraftServer} behind a weak key so it drops when an integrated
 * server is collected on world reload.
 */
public final class RoomPreviewService {

    /** How often (in ticks) loaded rooms are rescanned. 40 ticks ≈ 2 s — "every few ticks", not every tick. */
    public static final int GATHER_INTERVAL_TICKS = 40;

    /** How often (in ticks) live entities are captured — every tick, so clients see real per-tick motion. */
    public static final int ENTITY_INTERVAL_TICKS = 1;

    /**
     * The chunk ticket that keeps a watched room loaded <em>and ticking</em> so the preview shows live
     * motion. {@code FLAG_LOADING | FLAG_SIMULATION | FLAG_KEEP_DIMENSION_ACTIVE} (like vanilla's
     * player-simulation ticket) and non-persistent (never written to the save). Added with
     * {@link #PREVIEW_TICKET_RADIUS radius 2} it reaches the entity-ticking level (33 − 2 = 31), so
     * entities in the room actually move — which does mean the room fully simulates (AI, redstone,
     * random ticks, spawning) while it's being previewed.
     */
    private static final TicketType PREVIEW_TICKET = new TicketType(TicketType.NO_TIMEOUT,
            TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE);

    /** Radius that brings the ticketed chunk to the entity-ticking level (33 − radius = 31). */
    private static final int PREVIEW_TICKET_RADIUS = 2;

    private static final Map<MinecraftServer, ServerState> STATES = new WeakHashMap<>();

    private RoomPreviewService() {}

    // --- subscription management ------------------------------------------------------

    /**
     * Replaces {@code player}'s subscription with {@code roomCodes} and immediately sends any already
     * cached snapshots for rooms the player didn't previously have (so a client that comes into range
     * of a machine doesn't wait a full scan interval for its first frame).
     */
    public static void setSubscription(ServerPlayer player, List<String> roomCodes) {
        final MinecraftServer server = player.level().getServer();
        if (server == null) return;

        final ServerState state = STATES.computeIfAbsent(server, s -> new ServerState());
        final Set<String> requested = new HashSet<>(roomCodes);
        final Set<String> previous = state.byPlayer.getOrDefault(player.getUUID(), Set.of());

        state.subscribe(player.getUUID(), requested);
        CompactMachinesCore.modLog().info("[RoomPreview] subscription from {}: {}", player.getName().getString(), requested);

        // Push cached snapshots (and this player's spawn) for the rooms this player just added.
        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(server);
        for (String code : requested) {
            if (previous.contains(code)) continue;
            final CachedSnapshot cached = state.cache.get(code);
            if (cached != null)
                PacketDistributor.sendToPlayer(player, new RoomPreviewSnapshotPacket(code, cached.snapshot));
            if (rooms != null)
                rooms.get(code).ifPresent(room -> sendSpawnIfChanged(player, room, code, state));

            // An empty room sends no entity updates, so clear anything stale the client still holds
            // (e.g. themselves, from before they left it). Occupied rooms send every entity tick anyway.
            if (state.lastEntityCount.getOrDefault(code, 0) == 0)
                PacketDistributor.sendToPlayer(player, new RoomEntitiesPacket(code, List.of()));
        }
    }

    // --- ticking ----------------------------------------------------------------------

    public static void onLevelTick(final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(CompactDimension.LEVEL_KEY)) return;

        final MinecraftServer server = level.getServer();
        final ServerState state = STATES.get(server);
        if (state == null) return;

        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(server);
        if (rooms == null) return;

        // Keep watched rooms loaded (and release the rest) before trying to read them.
        reconcilePreviewTickets(level, rooms, state);
        if (state.byRoom.isEmpty()) return;

        final long now = level.getGameTime();
        final boolean doBlocks = now % GATHER_INTERVAL_TICKS == 0L;
        final boolean doEntities = now % ENTITY_INTERVAL_TICKS == 0L;
        if (!doBlocks && !doEntities) return;

        // Snapshot the subscribed-room set so a subscription change mid-loop can't disturb iteration.
        for (String code : new ArrayList<>(state.byRoom.keySet())) {
            final Set<UUID> subscribers = state.byRoom.get(code);
            if (subscribers == null || subscribers.isEmpty()) continue;

            final RoomInstance room = rooms.get(code).orElse(null);
            if (room == null || !isLoaded(level, room)) continue;

            if (doBlocks) {
                gatherBlocks(server, level, room, code, subscribers, state);
                for (UUID id : subscribers) {
                    final ServerPlayer player = server.getPlayerList().getPlayer(id);
                    if (player != null) sendSpawnIfChanged(player, room, code, state);
                }
            }
            if (doEntities) gatherEntities(server, level, room, code, subscribers, state);
        }
    }

    /**
     * Sends {@code player} their spawn (position and facing) in {@code room} — their own if set, else the room's default,
     * as the shrinking device resolves it — in the preview's room-local coordinates, if it changed
     * since last sent.
     */
    private static void sendSpawnIfChanged(ServerPlayer player, RoomInstance room, String code, ServerState state) {
        final var spawnManager = room.getCapability(RoomCapabilities.SPAWN_MANAGER);
        if (spawnManager == null) return;

        final var spawns = spawnManager.spawns();
        final var roomSpawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
        final var spawn = roomSpawn.position();

        // Same origin as RoomPreviewSnapshot#capture: the interior's minimum block corner
        final AABB inner = room.boundaries().innerBounds();
        final Vector3f local = new Vector3f(
                (float) (spawn.x - Mth.floor(inner.minX)),
                (float) (spawn.y - Mth.floor(inner.minY)),
                (float) (spawn.z - Mth.floor(inner.minZ)));

        // RoomSpawn rotation is (pitch, yaw), as Entity#getRotationVector
        final var packet = new RoomPreviewSpawnPacket(code, local, roomSpawn.rotation().y, roomSpawn.rotation().x);
        final var sent = state.lastSpawns.computeIfAbsent(player.getUUID(), u -> new HashMap<>());
        if (packet.equals(sent.get(code))) return;
        sent.put(code, packet);
        PacketDistributor.sendToPlayer(player, packet);
    }

    /** Captures the room's blocks and sends a snapshot only when the contents changed. */
    private static void gatherBlocks(MinecraftServer server, ServerLevel level, RoomInstance room,
                                     String code, Set<UUID> subscribers, ServerState state) {
        final RoomPreviewSnapshot snapshot = RoomPreviewSnapshot.capture(level, room.boundaries().innerBounds());
        final int hash = snapshot.contentHash();

        final CachedSnapshot cached = state.cache.get(code);
        if (cached != null && cached.hash == hash) return; // unchanged — don't resend

        state.cache.put(code, new CachedSnapshot(hash, snapshot));
        broadcast(server, subscribers, new RoomPreviewSnapshotPacket(code, snapshot));
    }

    /**
     * Captures the (non-player) entities in the room, in room-local space, and sends them live. Skips
     * sending repeated empties: an empty is sent only once, to clear the client when the last entity
     * leaves.
     */
    private static void gatherEntities(MinecraftServer server, ServerLevel level, RoomInstance room,
                                       String code, Set<UUID> subscribers, ServerState state) {
        final AABB inner = room.boundaries().innerBounds();
        final int ox = Mth.floor(inner.minX), oy = Mth.floor(inner.minY), oz = Mth.floor(inner.minZ);

        final List<RoomEntitySnapshot> snaps = new ArrayList<>();
        for (Entity e : level.getEntities((Entity) null, inner, entity -> !entity.isSpectator())) {
            // While previewed, keep mobs "awake" as if a player were nearby, so their wander/stroll
            // AI (which bails out once noActionTime ≥ 100 with no player around) keeps running.
            if (e instanceof net.minecraft.world.entity.Mob mob)
                mob.setNoActionTime(0);

            final float headYaw = e instanceof LivingEntity le ? le.getYHeadRot() : e.getYRot();
            final java.util.UUID playerId = e instanceof Player p ? p.getUUID() : null;
            final String playerName = e instanceof Player p ? p.getGameProfile().name() : null;
            snaps.add(new RoomEntitySnapshot(e.getId(), e.getType(),
                    (float) (e.getX() - ox), (float) (e.getY() - oy), (float) (e.getZ() - oz),
                    e.getYRot(), e.getXRot(), headYaw, e.onGround(), playerId, playerName));
        }

        final int previous = state.lastEntityCount.getOrDefault(code, 0);
        if (snaps.isEmpty() && previous == 0) return; // nothing there and nothing to clear
        state.lastEntityCount.put(code, snaps.size());
        broadcast(server, subscribers, new RoomEntitiesPacket(code, snaps));
    }

    /** {@return true if every chunk the room's interior spans is already loaded} */
    private static boolean isLoaded(ServerLevel level, RoomInstance room) {
        return room.boundaries().innerChunkPositions().allMatch(cp -> level.hasChunk(cp.x(), cp.z()));
    }

    /**
     * Adds preview load tickets for rooms that gained a watcher and removes them for rooms that lost
     * their last one, so exactly the currently-watched rooms are held loaded. Runs each gather tick;
     * a newly watched room becomes readable a cycle or two later, once its chunks finish loading.
     */
    private static void reconcilePreviewTickets(ServerLevel level, RoomRegistry rooms, ServerState state) {
        final Set<String> desired = state.byRoom.keySet();

        for (String code : new ArrayList<>(desired)) {
            if (state.ticketed.contains(code)) continue;
            final RoomInstance room = rooms.get(code).orElse(null);
            if (room == null) continue;
            setPreviewTickets(level, room, true);
            state.ticketed.add(code);
        }

        state.ticketed.removeIf(code -> {
            if (desired.contains(code)) return false;
            rooms.get(code).ifPresent(room -> setPreviewTickets(level, room, false));
            return true;
        });
    }

    private static void setPreviewTickets(ServerLevel level, RoomInstance room, boolean add) {
        final var chunkSource = level.getChunkSource();
        room.boundaries().innerChunkPositions().forEach(cp -> {
            if (add) chunkSource.addTicketWithRadius(PREVIEW_TICKET, cp, PREVIEW_TICKET_RADIUS);
            else chunkSource.removeTicketWithRadius(PREVIEW_TICKET, cp, PREVIEW_TICKET_RADIUS);
        });
    }

    private static void broadcast(MinecraftServer server, Set<UUID> subscribers, CustomPacketPayload packet) {
        for (UUID id : subscribers) {
            final ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null)
                PacketDistributor.sendToPlayer(player, packet);
        }
    }

    // --- lifecycle cleanup ------------------------------------------------------------

    public static void onPlayerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        final MinecraftServer server = player.level().getServer();
        if (server == null) return;
        final ServerState state = STATES.get(server);
        if (state != null)
            state.unsubscribe(player.getUUID());
    }

    public static void onServerStopping(final ServerStoppingEvent event) {
        STATES.remove(event.getServer());
    }

    // --- per-server state -------------------------------------------------------------

    private record CachedSnapshot(int hash, RoomPreviewSnapshot snapshot) {}

    private static final class ServerState {
        /** Player → the rooms they're subscribed to. */
        private final Map<UUID, Set<String>> byPlayer = new HashMap<>();
        /** Reverse index: room code → the players subscribed to it (drives sends). */
        private final Map<String, Set<UUID>> byRoom = new HashMap<>();
        /** Latest sent snapshot per room, with its content hash, so unchanged rooms aren't re-sent. */
        private final Map<String, CachedSnapshot> cache = new HashMap<>();
        /** Rooms currently held loaded by a preview ticket (see {@link #reconcilePreviewTickets}). */
        private final Set<String> ticketed = new HashSet<>();
        /** Last entity count sent per room, so repeated empties aren't re-sent. */
        private final Map<String, Integer> lastEntityCount = new HashMap<>();
        /** Last spawn sent per player and room, so unchanged spawns aren't re-sent. */
        private final Map<UUID, Map<String, RoomPreviewSpawnPacket>> lastSpawns = new HashMap<>();

        void subscribe(UUID player, Set<String> rooms) {
            unsubscribe(player);
            if (rooms.isEmpty()) return;
            byPlayer.put(player, rooms);
            for (String code : rooms)
                byRoom.computeIfAbsent(code, c -> new HashSet<>()).add(player);
        }

        void unsubscribe(UUID player) {
            lastSpawns.remove(player);
            final Set<String> previous = byPlayer.remove(player);
            if (previous == null) return;
            for (String code : previous) {
                final Set<UUID> subs = byRoom.get(code);
                if (subs == null) continue;
                subs.remove(player);
                if (subs.isEmpty()) {
                    byRoom.remove(code);
                    cache.remove(code); // no one watching — drop the cached snapshot too
                    lastEntityCount.remove(code);
                }
            }
        }
    }
}
