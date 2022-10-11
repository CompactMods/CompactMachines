package dev.compactmods.machines.room;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.room.IRoomHistory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public class RoomCapabilities {

    public static final Capability<IRoomHistory> ROOM_HISTORY = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    void onCapRegistration(final RegisterCapabilitiesEvent evt) {
        evt.register(IRoomHistory.class);
    }
}
