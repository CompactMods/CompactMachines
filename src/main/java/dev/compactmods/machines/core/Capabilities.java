package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.rooms.capability.IRoomHistory;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class Capabilities {

    @SubscribeEvent
    void onCapRegistration(final RegisterCapabilitiesEvent evt) {
        evt.register(IRoomHistory.class);
    }
}
