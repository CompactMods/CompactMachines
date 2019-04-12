package org.dave.compactmachines3.misc;

import net.minecraft.entity.monster.EntityShulker;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MachineEventHandler {
    @SubscribeEvent
    public static void onEnderTeleport(EnderTeleportEvent event) {
        if(!(event.getEntity() instanceof EntityShulker)) {
            return;
        }

        if(event.getEntity().world.isRemote) {
            return;
        }

        if(event.getEntity().world.provider.getDimension() != ConfigurationHandler.Settings.dimensionId) {
            return;
        }

        event.setCanceled(true);
    }
}
