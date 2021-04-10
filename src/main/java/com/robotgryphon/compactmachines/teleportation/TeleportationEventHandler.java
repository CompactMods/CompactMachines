package com.robotgryphon.compactmachines.teleportation;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.core.Registration;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.EntityTeleportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class TeleportationEventHandler {

    @SubscribeEvent
    public static void onEntityTeleport(final EntityTeleportEvent evt) {
        // Allow teleport commands, we don't want to trap people anywhere
        if(evt instanceof EntityTeleportEvent.TeleportCommand)
            return;

        // Allow ender pearls as well, since players can teleport around inside machines
        if(evt instanceof EntityTeleportEvent.EnderPearl)
            return;

        // Make sure we only target player entities on a server
        Entity entity = evt.getEntity();
        if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity sp = (ServerPlayerEntity) entity;
            if(sp.level.dimension() == Registration.COMPACT_DIMENSION) {
                if(evt.isCancelable()) {
                    evt.setCanceled(true);
                    return;
                }
                
                // If the event isn't cancelable, force the position to
                // be the same as the starting point
                Vector3d prev = evt.getPrev();
                evt.setTargetX(prev.x);
                evt.setTargetY(prev.y);
                evt.setTargetZ(prev.z);
            }
        }
    }
}
