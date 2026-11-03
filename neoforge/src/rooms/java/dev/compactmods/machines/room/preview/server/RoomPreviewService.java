package dev.compactmods.machines.room.preview.server;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.sync.BlockEntitySyncHooks;
import dev.compactmods.machines.room.network.RoomBlockEntitiesPacket;
import dev.compactmods.machines.room.network.RoomEntitiesPacket;
import dev.compactmods.machines.room.network.RoomPreviewSnapshotPacket;
import dev.compactmods.machines.room.network.RoomPreviewSpawnPacket;
import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private static final List<RoomPreviewContributor> CONTRIBUTORS = new CopyOnWriteArrayList<>();

    /** Adds a source of extra per-room preview data. Call during mod construction. */
    public static void addContributor(RoomPreviewContributor contributor) {
        CONTRIBUTORS.add(contributor);
    }

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
            if (rooms != null) {
                rooms.get(code).ifPresent(room -> {
                    sendSpawnIfChanged(player, room, code, state);
                    sendContributions(server, room, code, state, player);
                });
            }

            // Block entity data newer than the cached snapshot
            final Map<BlockPos, RoomBlockEntitySnapshot> tags = state.blockEntityTags.get(code);
            if (tags != null && !tags.isEmpty())
                PacketDistributor.sendToPlayer(player, new RoomBlockEntitiesPacket(code, List.copyOf(tags.values())));

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

        // This tick's reported block entity changes, taken whole so new reports start a fresh set
        final Set<BlockPos> dirtyBlocks = state.dirtyBlocks.isEmpty() ? Set.of() : Set.copyOf(state.dirtyBlocks);
        final Set<Long> dirtyChunks = state.dirtyChunks.isEmpty() ? Set.of() : Set.copyOf(state.dirtyChunks);
        state.dirtyBlocks.clear();
        state.dirtyChunks.clear();

        // Snapshot the subscribed-room set so a subscription change mid-loop can't disturb iteration.
        for (String code : new ArrayList<>(state.byRoom.keySet())) {
            final Set<UUID> subscribers = state.byRoom.get(code);
            if (subscribers == null || subscribers.isEmpty()) continue;

            final RoomInstance room = rooms.get(code).orElse(null);
            if (room == null || !isLoaded(level, room)) continue;

            if (doBlocks) {
                gatherBlocks(server, level, room, code, subscribers, state);
                sendContributions(server, room, code, state, null);
                for (UUID id : subscribers) {
                    final ServerPlayer player = server.getPlayerList().getPlayer(id);
                    if (player != null) sendSpawnIfChanged(player, room, code, state);
                }
            }
            if (doEntities) {
                gatherEntities(server, level, room, code, subscribers, state);
                if (!dirtyBlocks.isEmpty() || !dirtyChunks.isEmpty())
                    gatherBlockEntities(server, level, room, code, subscribers, state, dirtyBlocks, dirtyChunks);
            }
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
     * Captures every {@link RoomPreviewContributor}'s payload for the room and sends the ones that
     * changed since last sent to all of the room's watchers. A {@code newcomer} also gets the unchanged ones.
     */
    private static void sendContributions(MinecraftServer server, RoomInstance room, String code,
                                          ServerState state, @Nullable ServerPlayer newcomer) {
        final ServerLevel level = server.getLevel(CompactDimension.LEVEL_KEY);
        if (level == null || CONTRIBUTORS.isEmpty()) return;

        final Map<RoomPreviewContributor, CustomPacketPayload> sent =
                state.contributions.computeIfAbsent(code, c -> new HashMap<>());
        for (RoomPreviewContributor contributor : CONTRIBUTORS) {
            contributor.capture(level, room).ifPresent(payload -> {
                final boolean changed = !payload.equals(sent.put(contributor, payload));
                if (changed)
                    broadcast(server, state.byRoom.getOrDefault(code, Set.of()), payload);
                else if (newcomer != null)
                    PacketDistributor.sendToPlayer(newcomer, payload);
            });
        }
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

    /**
     * Sends the sync tags of the room's block entities that changed since last sent, out of those
     * {@linkplain BlockEntitySyncHooks reported} as changed this tick: by position (changed or synced the
     * vanilla way), or by chunk (a mod's own packet to the chunk's trackers, as Mekanism sends for items in
     * pipes). Reactive, so idle rooms cost nothing; the comparison only stops resending data that didn't
     * actually change.
     */
    private static void gatherBlockEntities(MinecraftServer server, ServerLevel level, RoomInstance room,
                                            String code, Set<UUID> subscribers, ServerState state,
                                            Set<BlockPos> dirtyBlocks, Set<Long> dirtyChunks) {
        final AABB inner = room.boundaries().innerBounds();
        final Set<BlockPos> candidates = new HashSet<>();
        for (BlockPos pos : dirtyBlocks) {
            if (inner.contains(Vec3.atCenterOf(pos)))
                candidates.add(pos);
        }
        if (!dirtyChunks.isEmpty()) {
            room.boundaries().innerChunkPositions()
                    .filter(cp -> dirtyChunks.contains(ChunkPos.pack(cp.x(), cp.z())))
                    .forEach(cp -> level.getChunk(cp.x(), cp.z()).getBlockEntities().keySet().stream()
                            .filter(pos -> inner.contains(Vec3.atCenterOf(pos)))
                            .forEach(candidates::add));
        }
        if (candidates.isEmpty()) return;

        final BlockPos origin = BlockPos.containing(inner.minX, inner.minY, inner.minZ);
        final Map<BlockPos, RoomBlockEntitySnapshot> sent = state.blockEntityTags.computeIfAbsent(code, c -> new HashMap<>());
        final List<RoomBlockEntitySnapshot> changed = new ArrayList<>();
        for (BlockPos pos : candidates) {
            final BlockPos local = pos.subtract(origin);
            final BlockEntity be = level.getBlockEntity(pos);
            if (be == null || be.isRemoved()) {
                // Gone ones leave with the next block snapshot; forget them so they're sent again if they come back
                sent.remove(local);
                continue;
            }

            final var current = new RoomBlockEntitySnapshot(local, be.getType(), be.getUpdateTag(level.registryAccess()));
            if (!current.equals(sent.get(local))) {
                sent.put(local, current);
                changed.add(current);
            }
        }

        if (!changed.isEmpty())
            broadcast(server, subscribers, new RoomBlockEntitiesPacket(code, changed));
    }

    /** Collects reported block entity changes in the compact dimension, while any room is watched. */
    private static final BlockEntitySyncHooks.Listener SYNC_LISTENER = new BlockEntitySyncHooks.Listener() {
        @Override
        public void blockChanged(ServerLevel level, BlockPos pos) {
            final ServerState state = watching(level);
            if (state != null)
                state.dirtyBlocks.add(pos.immutable());
        }

        @Override
        public void chunkSynced(ServerLevel level, ChunkPos chunk) {
            final ServerState state = watching(level);
            if (state != null)
                state.dirtyChunks.add(ChunkPos.pack(chunk.x(), chunk.z()));
        }

        private static @Nullable ServerState watching(ServerLevel level) {
            if (!level.dimension().equals(CompactDimension.LEVEL_KEY))
                return null;
            final ServerState state = STATES.get(level.getServer());
            return state == null || state.byRoom.isEmpty() ? null : state;
        }
    };

    /** Starts listening for block entity changes. Call during mod construction. */
    public static void init() {
        BlockEntitySyncHooks.setListener(SYNC_LISTENER);
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
        /** Block entities reported changed since the last tick, by world position (see {@link BlockEntitySyncHooks}). */
        private final Set<BlockPos> dirtyBlocks = ConcurrentHashMap.newKeySet();
        /** Chunks a mod sent its own packets to since the last tick, packed. */
        private final Set<Long> dirtyChunks = ConcurrentHashMap.newKeySet();
        /** Last block entity data sent per room, by room-local position, so unchanged ones aren't re-sent. */
        private final Map<String, Map<BlockPos, RoomBlockEntitySnapshot>> blockEntityTags = new HashMap<>();
        /** Last payload sent per room and contributor, so unchanged ones aren't re-sent. */
        private final Map<String, Map<RoomPreviewContributor, CustomPacketPayload>> contributions = new HashMap<>();
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
                    contributions.remove(code);
                    blockEntityTags.remove(code);
                }
            }
        }
    }
}
