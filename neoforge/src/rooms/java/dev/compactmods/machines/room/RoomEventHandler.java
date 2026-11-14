package dev.compactmods.machines.room;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.Translations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.apache.logging.log4j.Logger;

public class RoomEventHandler {

    private static final Logger LOGS = CompactMachinesCore.modLog();

    /**
     * Handles an entity that has moved to a non-CM dimension, clearing their history
     * @param dimensionEvent
     */
    public static void entityChangedDimensions(final EntityTravelToDimensionEvent dimensionEvent) {
        final var ent = dimensionEvent.getEntity();
        if(!(ent instanceof Player p)) return;

        if(!CompactDimension.isLevelCompact(dimensionEvent.getDimension())) {
            if(p instanceof ServerPlayer sp) {
                LOGS.debug("Resetting player {}'s room history due to dimension change.", sp.getGameProfile().name());
                // FIXME
//                PlayerUtil.resetPlayerHistory(sp);
            }
        }
    }

    public static void entityJoined(final EntityJoinLevelEvent evt) {
        Entity ent = evt.getEntity();

        // no-op clients and non-compact dimensions, we only care about server spawns
        if (!CompactDimension.isLevelCompact(ent.level()) || ent.level().isClientSide())
            return;

        // Handle players
        if (ent instanceof ServerPlayer serverPlayer) {
            // FIXME sync current room info to client player
//            final var roomProvider = CompactRoomProvider.instance(serverPlayer.getLevel());
//            roomProvider.findByChunk(serverPlayer.chunkPosition()).ifPresent(roomInfo -> {
//                CompactMachinesNet.CHANNEL.send(
//                        PacketDistributor.PLAYER.with(() -> serverPlayer),
//                        new SyncRoomMetadataPacket(roomInfo.roomCode(), roomInfo.owner(roomProvider))
//                );
//            });
        } else {
            if (!positionInsideRoom(ent, ent.position())) {
                evt.setCanceled(true);
            }
        }
    }

    public static void checkSpawn(final FinalizeSpawnEvent evt) {
        Vec3 target = new Vec3(evt.getX(), evt.getY(), evt.getZ());

        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if (!CompactDimension.isLevelCompact(ent.level())) return;

        if (!positionInsideRoom(ent, target))
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
     * @return True if position is inside a room; false otherwise.
     */
    private static boolean positionInsideRoom(Entity entity, Vec3 target) {
        final var level = entity.level();
        if(!CompactDimension.isLevelCompact(entity.level()))
            return false;

        return level.getServer()
                .getCapability(RoomCapabilities.CHUNK_MANAGER)
                .findRoomByChunk(entity.chunkPosition())
                .map(ib -> ib.boundaries().innerBounds().contains(target))
                .orElse(false);
    }

    private static void doEntityTeleportHandle(EntityTeleportEvent evt, Vec3 target, Entity ent) {
        if (!positionInsideRoom(ent, target)) {
            if (ent instanceof ServerPlayer sp) {
                sp.sendOverlayMessage(Translations.TELEPORT_OUT_OF_BOUNDS.get());
            }

            evt.setCanceled(true);
        }
    }
}
