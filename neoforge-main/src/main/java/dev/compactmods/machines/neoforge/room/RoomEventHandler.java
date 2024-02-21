package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class RoomEventHandler {

    @SubscribeEvent
    public static void entityJoined(final EntityJoinLevelEvent evt) {
        Entity ent = evt.getEntity();

        boolean isPlayer = ent instanceof Player;
        boolean isCompact = CompactDimension.isLevelCompact(ent.level());

        // no-op clients and non-compact dimensions, we only care about server spawns
        if (!isCompact || ent.level().isClientSide)
            return;

        // Handle players
        if (isPlayer && ent instanceof ServerPlayer serverPlayer) {
            // FIXME sync current room info to client player
//            final var roomProvider = CompactRoomProvider.instance(serverPlayer.getLevel());
//            roomProvider.findByChunk(serverPlayer.chunkPosition()).ifPresent(roomInfo -> {
//                CompactMachinesNet.CHANNEL.send(
//                        PacketDistributor.PLAYER.with(() -> serverPlayer),
//                        new SyncRoomMetadataPacket(roomInfo.code(), roomInfo.owner(roomProvider))
//                );
//            });
        } else {
            if (!positionInsideRoom(ent, ent.position())) {
                evt.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(final MobSpawnEvent.FinalizeSpawn evt) {
        Vec3 target = new Vec3(evt.getX(), evt.getY(), evt.getZ());

        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if (!ent.level().dimension().equals(CompactDimension.LEVEL_KEY)) return;

        if (!positionInsideRoom(ent, target))
            evt.setSpawnCancelled(true);
    }

    @SubscribeEvent
    public static void onEntityTeleport(final EntityTeleportEvent evt) {
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
        if (!level.dimension().equals(CompactDimension.LEVEL_KEY)) return false;

        if (level instanceof ServerLevel compactDim) {
            ChunkPos playerChunk = entity.chunkPosition();
            return true;
            // FIXME
//            return Rooms.chunkManager().findRoomByChunk(playerChunk)
//                    .map(reg -> reg.)
//                    .map(ib -> ib.contains(target))
//                    .orElse(false);
        }

        return false;
    }

    private static void doEntityTeleportHandle(EntityTeleportEvent evt, Vec3 target, Entity ent) {
        if (!positionInsideRoom(ent, target)) {
            if (ent instanceof ServerPlayer) {
                ((ServerPlayer) ent).displayClientMessage(TranslationUtil.message(Messages.TELEPORT_OUT_OF_BOUNDS, ent.getName()).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            }
            evt.setCanceled(true);
        }
    }
}
