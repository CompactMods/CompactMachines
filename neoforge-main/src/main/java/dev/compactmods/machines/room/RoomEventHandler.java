package dev.compactmods.machines.room;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.room.RoomDebugInformation;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.i18n.Translations;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.util.MathUtil;
import dev.compactmods.machines.util.PlayerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class RoomEventHandler {

    private static final Logger LOGS = LoggingUtil.modLog();

    /**
     * How far outside a room's inner bounds an item can spawn and still be pulled back inside.
     */
    private static final double ITEM_RESCUE_DISTANCE = 5;

    /**
     * @param inside True if the checked position is inside the entity's current room.
     * @param closestRoom The room nearest to the checked position, if any are within {@link #ITEM_RESCUE_DISTANCE}.
     */
    private record RoomPositionCheck(boolean inside, Optional<RoomInstance> closestRoom) {
        static final RoomPositionCheck OUTSIDE = new RoomPositionCheck(false, Optional.empty());
    }

    /**
     * Handles an entity that has moved to a non-CM dimension, clearing their history
     * @param dimensionEvent
     */
    public static void entityChangedDimensions(final EntityTravelToDimensionEvent dimensionEvent) {
        final var ent = dimensionEvent.getEntity();
        if(!(ent instanceof Player p)) return;

        if(!CompactDimension.isLevelCompact(dimensionEvent.getDimension())) {
            if(p instanceof ServerPlayer sp) {
                LOGS.debug("Resetting player {}'s room history due to dimension change.", sp.getGameProfile().getName());
                PlayerUtil.resetPlayerHistory(sp);
            }
        }
    }

    public static void entityJoined(final EntityJoinLevelEvent evt) {
        Entity ent = evt.getEntity();

        // no-op clients and non-compact dimensions, we only care about server spawns
        if (!CompactDimension.isLevelCompact(ent.level()) || ent.level().isClientSide)
            return;

        // Handle players
        if (ent instanceof ServerPlayer serverPlayer) {
            CompactMachines.chunkManager()
                    .findRoomByChunk(serverPlayer.chunkPosition())
                    .flatMap((String roomCode) -> CompactMachines.room(serverPlayer.server, roomCode))
                    .ifPresent(room -> {
                        serverPlayer.setData(CMDataAttachments.CURRENT_ROOM_CODE, room.code());
                        serverPlayer.setData(CMDataAttachments.CURRENT_ROOM_DEBUG_INFO,
                                new RoomDebugInformation(room.code(), room.getData(CMDataAttachments.ROOM_OWNER)));
                    });
        } else {
            final var check = positionInsideRoom(ent, ent.position());
            if (!check.inside()) {
                if (ent instanceof ItemEntity item && check.closestRoom().isPresent()) {
                    final var inner = check.closestRoom().get().boundaries().innerBounds();
                    if (inner.distanceToSqr(item.position()) <= ITEM_RESCUE_DISTANCE * ITEM_RESCUE_DISTANCE) {
                        final double halfWidth = item.getBbWidth() / 2;
                        item.setPos(
                                Mth.clamp(item.getX(), inner.minX + halfWidth, inner.maxX - halfWidth),
                                Mth.clamp(item.getY(), inner.minY, inner.maxY - item.getBbHeight()),
                                Mth.clamp(item.getZ(), inner.minZ + halfWidth, inner.maxZ - halfWidth));
                        return;
                    }
                }

                evt.setCanceled(true);
            }
        }
    }

    public static void checkSpawn(final FinalizeSpawnEvent evt) {
        Vec3 target = new Vec3(evt.getX(), evt.getY(), evt.getZ());

        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if (!CompactDimension.isLevelCompact(ent.level())) return;

        if (!positionInsideRoom(ent, target).inside())
            evt.setSpawnCancelled(true);
    }

    public static void entityTeleport(final EntityTeleportEvent evt) {
        // Allow teleport commands, we don't want to trap people anywhere
        if (evt instanceof EntityTeleportEvent.TeleportCommand) return;
        if (!evt.getEntity().level().dimension().equals(CompactDimension.LEVEL_KEY)) return;

        Entity ent = evt.getEntity();
        doEntityTeleportHandle(evt, evt.getTarget(), ent);
    }


    /**
     * Helper to determine if an event should be canceled, by determining if a target is outside
     * a machine's bounds.
     *
     * @param entity Entity trying to teleport.
     * @param target Teleportation target location.
     * @return Whether the position is inside the entity's current room, along with the closest room to the target.
     */
    private static RoomPositionCheck positionInsideRoom(Entity entity, Vec3 target) {
        if (!CompactDimension.isLevelCompact(entity.level())) return RoomPositionCheck.OUTSIDE;

        final var chunks = CompactMachines.chunkManager();
        final var server = entity.getServer();

        final var currentRoom = chunks.findRoomByChunk(entity.chunkPosition())
                .flatMap((String roomCode) -> CompactMachines.room(server, roomCode));

        if (currentRoom.map(room -> room.boundaries().innerBounds().contains(target)).orElse(false))
            return new RoomPositionCheck(true, currentRoom);

        // Room chunks only cover the outer bounds, so search nearby chunks as well
        final var searchArea = new AABB(target, target).inflate(ITEM_RESCUE_DISTANCE);
        final var closest = Stream.concat(
                        currentRoom.map(RoomInstance::code).stream(),
                        MathUtil.getChunksFromAABB(searchArea).flatMap(chunk -> chunks.findRoomByChunk(chunk).stream()))
                .distinct()
                .flatMap(roomCode -> CompactMachines.room(server, roomCode).stream())
                .min(Comparator.comparingDouble(room -> room.boundaries().innerBounds().distanceToSqr(target)));

        return new RoomPositionCheck(false, closest);
    }

    private static void doEntityTeleportHandle(EntityTeleportEvent evt, Vec3 target, Entity ent) {
        if (!positionInsideRoom(ent, target).inside()) {
            if (ent instanceof ServerPlayer sp) {
                sp.displayClientMessage(Translations.TELEPORT_OUT_OF_BOUNDS.get(), true);
            }

            evt.setCanceled(true);
        }
    }
}
