package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.capability.CompactRoomCapability;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class BasicRoomCapabilities {

    public static void register(RegisterCapabilitiesEvent event) {

        CompactRoomCapability.register(RoomCapabilities.ROOM_DATA_ATTACHMENTS, (server, roomCode, v)
                -> CompactMachines.roomData(roomCode));

        CompactRoomCapability.register(RoomCapabilities.UPGRADE_DATA_ATTACHMENTS, (server, roomCode, upgradeId)
                -> CompactMachines.roomUpgradeData(roomCode, upgradeId));
    }
}
