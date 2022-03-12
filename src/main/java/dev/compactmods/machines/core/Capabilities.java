package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.room.capability.IRoomHistory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class Capabilities {

    public static final Capability<IMachineRoom> ROOM = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final Capability<IRoomHistory> ROOM_HISTORY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    void onCapRegistration(final RegisterCapabilitiesEvent evt) {
        evt.register(IMachineRoom.class);
        evt.register(IRoomHistory.class);
    }
}
