package dev.compactmods.machines.room;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class RoomEventHandler {

    @SubscribeEvent
    public static void entityJoined(final EntityJoinLevelEvent evt) {
        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if ((ent instanceof Player) || !ent.level.dimension().equals(CompactDimension.LEVEL_KEY)) return;

        // no-op clients, we only care about blocking server spawns
        if(ent.level.isClientSide) return;

        if (!positionInsideRoom(ent, ent.position())) {
            evt.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onCheckSpawn(final LivingSpawnEvent.CheckSpawn evt) {
        Vec3 target = new Vec3(evt.getX(), evt.getY(), evt.getZ());

        Entity ent = evt.getEntity();

        // Early exit if spawning in non-CM dimensions
        if (!ent.level.dimension().equals(CompactDimension.LEVEL_KEY)) return;

        if (!positionInsideRoom(ent, target)) evt.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onEntityTeleport(final EntityTeleportEvent evt) {
        // Allow teleport commands, we don't want to trap people anywhere
        if (evt instanceof EntityTeleportEvent.TeleportCommand) return;
        if(!evt.getEntity().level.dimension().equals(CompactDimension.LEVEL_KEY)) return;

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
        final var level = entity.level;
        if (!level.dimension().equals(CompactDimension.LEVEL_KEY)) return false;

        if (level instanceof ServerLevel compactDim) {
            ChunkPos playerChunk = entity.chunkPosition();

            final var roomInfo = CompactRoomProvider.instance(compactDim);
            return roomInfo.isRoomChunk(playerChunk)
                ? roomInfo.findByChunk(playerChunk)
                        .map(IRoomRegistration::innerBounds)
                        .map(ib -> ib.contains(target))
                        .orElse(false)
                : false;
        }

        return false;
    }

    private static void doEntityTeleportHandle(EntityEvent evt, Vec3 target, Entity ent) {
        if (!positionInsideRoom(ent, target) && evt.isCancelable()) {
            if (ent instanceof ServerPlayer) {
                ((ServerPlayer) ent).displayClientMessage(TranslationUtil.message(Messages.TELEPORT_OUT_OF_BOUNDS, ent.getName()).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.ITALIC), true);
            }
            evt.setCanceled(true);
        }
    }
}
