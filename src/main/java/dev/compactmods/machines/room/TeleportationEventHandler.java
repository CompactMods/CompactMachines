package dev.compactmods.machines.room;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class TeleportationEventHandler {

    @SubscribeEvent
    public static void onEnderTeleport(final EntityTeleportEvent.EnderEntity evt) {
        Vec3 target = new Vec3(
                evt.getTargetX(),
                evt.getTargetY(),
                evt.getTargetZ()
        );

        Entity ent = evt.getEntity();
        doEntityTeleportHandle(evt, target, ent);
    }

    @SubscribeEvent
    public static void onEntityTeleport(final EntityTeleportEvent evt) {
        // Allow teleport commands, we don't want to trap people anywhere
        if (evt instanceof EntityTeleportEvent.TeleportCommand)
            return;

        Entity ent = evt.getEntity();
        doEntityTeleportHandle(evt, evt.getTarget(), ent);
    }


    /**
     * Helper to determine if an event should be canceled, by determining if a target is outside
     * a machine's bounds.
     *
     * @param entity Entity trying to teleport.
     * @param target Teleportation target location.
     * @return True if teleportation should be cancelled; false otherwise.
     */
    private static boolean cancelOutOfBoxTeleport(Entity entity, Vec3 target) {
        final var level = entity.level;
        if (!level.dimension().equals(Registration.COMPACT_DIMENSION))
            return false;

        if(level instanceof ServerLevel compactDim) {
            ChunkPos machineChunk = new ChunkPos(entity.chunkPosition().x, entity.chunkPosition().z);

            try {
                final CompactRoomData intern = CompactRoomData.get(compactDim);
                return !intern.getBounds(machineChunk).contains(target);
            } catch (NonexistentRoomException e) {
                CompactMachines.LOGGER.error(e);
                return false;
            }
        }

        return false;
    }

    private static void doEntityTeleportHandle(EntityEvent evt, Vec3 target, Entity ent) {
        if (ent.level.dimension() == Registration.COMPACT_DIMENSION) {
            if (cancelOutOfBoxTeleport(ent, target) && evt.isCancelable()) {
                if (ent instanceof ServerPlayer) {
                    ((ServerPlayer) ent).displayClientMessage(
                            TranslationUtil.message(Messages.TELEPORT_OUT_OF_BOUNDS, ent.getName())
                                    .withStyle(ChatFormatting.RED)
                                    .withStyle(ChatFormatting.ITALIC),
                            true
                    );
                }
                evt.setCanceled(true);
            }
        }
    }
}
