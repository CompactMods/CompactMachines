package dev.compactmods.machines.room.preview.client;

import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-side pool of throwaway {@link Entity} instances used only to <em>render</em> the live
 * entities in a room preview — driven through the entities' own {@code tick()} so their real
 * animation state (walk cycle, parrot wing-flap, and every other per-type animation the normal
 * client tick updates) plays, rather than a hand-rolled approximation.
 *
 * <p>Each server tick's snapshot is stored per {@code (roomCode, entityId)}; once per client tick
 * {@link #tickAll} applies the latest snapshot (position, rotation, on-ground, per-tick velocity) and
 * calls {@link Entity#tick()}. The instances are silenced and gravity-free so ticking has no audible
 * or physical side effects, and each tick is guarded so a misbehaving type can't crash the frame. The
 * renderer then interpolates the ticked entity by partial tick. Client render thread only.
 */
public final class RoomPreviewEntities {

    private static final Map<String, Map<Integer, Tracked>> BY_ROOM = new HashMap<>();

    private RoomPreviewEntities() {}

    /** Stores the latest snapshot (packet thread); creates the render instance on first sight. */
    public static void update(String roomCode, RoomEntitySnapshot snapshot) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        final Map<Integer, Tracked> perRoom = BY_ROOM.computeIfAbsent(roomCode, k -> new HashMap<>());
        final Tracked tracked = perRoom.get(snapshot.entityId());
        if (tracked == null || tracked.entity.getType() != snapshot.type()) {
            final Entity created = createEntity(mc, snapshot);
            if (created == null) return;
            created.setId(snapshot.entityId()); // renderers read getId() as a model seed
            created.setSilent(true);            // no ambient sounds while we tick it
            created.setNoGravity(true);         // no physics; we position it ourselves
            created.setPos(snapshot.x(), snapshot.y(), snapshot.z());
            perRoom.put(snapshot.entityId(), new Tracked(created, snapshot));
        } else {
            tracked.snapshot = snapshot;
        }
    }

    /** Players need a {@link RemotePlayer} (with their profile/skin); everything else is a plain type. */
    private static @Nullable Entity createEntity(Minecraft mc, RoomEntitySnapshot snapshot) {
        if (snapshot.playerId() != null && snapshot.playerName() != null)
            return new RemotePlayer(mc.level, new com.mojang.authlib.GameProfile(snapshot.playerId(), snapshot.playerName()));
        return snapshot.type().create(mc.level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true));
    }

    /** Ticks every preview entity once, applying its latest snapshot first. Call once per client tick. */
    public static void tickAll() {
        for (Map<Integer, Tracked> perRoom : BY_ROOM.values()) {
            for (Tracked tracked : perRoom.values()) {
                final Entity entity = tracked.entity;
                final RoomEntitySnapshot s = tracked.snapshot;

                // "Previous" pose = last applied — the start point the renderer interpolates from.
                final double ox = entity.getX(), oy = entity.getY(), oz = entity.getZ();
                entity.setOldPosAndRot();
                if (entity instanceof LivingEntity living) {
                    living.yHeadRotO = living.yHeadRot;
                    living.yBodyRotO = living.yBodyRot;
                }

                // Feed the tick the new pose + motion so its animations (walk cycle, parrot flap, …)
                // compute from real movement.
                applyPose(entity, s);
                entity.setDeltaMovement(new Vec3(s.x() - ox, s.y() - oy, s.z() - oz));

                try {
                    entity.tick();
                } catch (Throwable ignored) {
                    // A type that misbehaves when ticked outside a real level just renders unanimated.
                }

                // Re-assert the snapshot pose: the mob's own head-turn/body-follow logic during tick()
                // would otherwise fight the server's rotation and make heads jitter. Animation state
                // (walkAnimation, flap, tickCount) is separate and survives this.
                applyPose(entity, s);
            }
        }
    }

    private static void applyPose(Entity entity, RoomEntitySnapshot s) {
        entity.setPos(s.x(), s.y(), s.z());
        entity.setYRot(s.yaw());
        entity.setXRot(s.pitch());
        entity.setYHeadRot(s.headYaw());
        entity.setYBodyRot(s.yaw());
        entity.setOnGround(s.onGround());
    }

    /** {@return the render instance for {@code entityId} in {@code roomCode}, or {@code null}} */
    public static @Nullable Entity get(String roomCode, int entityId) {
        final Map<Integer, Tracked> perRoom = BY_ROOM.get(roomCode);
        return perRoom == null ? null : (perRoom.get(entityId) == null ? null : perRoom.get(entityId).entity);
    }

    /** Drops cached instances (and rooms) no longer present in the live snapshots. */
    public static void retainCurrent() {
        BY_ROOM.entrySet().removeIf(roomEntry -> {
            final var current = ClientRoomEntities.get(roomEntry.getKey());
            if (current.isEmpty()) return true;
            final Set<Integer> ids = new HashSet<>();
            for (RoomEntitySnapshot s : current) ids.add(s.entityId());
            roomEntry.getValue().keySet().retainAll(ids);
            return roomEntry.getValue().isEmpty();
        });
    }

    public static void clear() {
        BY_ROOM.clear();
    }

    private static final class Tracked {
        final Entity entity;
        RoomEntitySnapshot snapshot;

        Tracked(Entity entity, RoomEntitySnapshot snapshot) {
            this.entity = entity;
            this.snapshot = snapshot;
        }
    }
}
