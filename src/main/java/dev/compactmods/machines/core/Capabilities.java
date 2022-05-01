package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.IRoomInformation;
import dev.compactmods.machines.api.room.IRoomHistory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class Capabilities {

    public static final Capability<IRoomInformation> ROOM = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IRoomHistory> ROOM_HISTORY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    void onCapRegistration(final RegisterCapabilitiesEvent evt) {
        evt.register(IRoomInformation.class);
        evt.register(IRoomHistory.class);
    }
}
