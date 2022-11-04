package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.room.IRoomHistory;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class RoomCapabilities {

    public static final Capability<IRoomHistory> ROOM_HISTORY = CapabilityManager.get(new CapabilityToken<>() {
    });


}
