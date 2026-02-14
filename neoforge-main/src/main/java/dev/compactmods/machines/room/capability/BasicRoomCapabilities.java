package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.capability.CompactRoomCapability;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.stream.Stream;

public class BasicRoomCapabilities {

    public static void register(RegisterCapabilitiesEvent ignoredEvent) {

        CompactRoomCapability.register(RoomCapabilities.ROOM_DATA_ATTACHMENTS, (server, roomCode, v)
                -> CompactMachines.roomData(roomCode));

        CompactRoomCapability.register(RoomCapabilities.UPGRADE_DATA_ATTACHMENTS, (server, roomCode, upgradeId)
                -> CompactMachines.roomUpgradeData(roomCode, upgradeId));

        CompactRoomCapability.register(RoomCapabilities.UPGRADES, ((server, roomCode, v) -> {
            final var room = CompactMachines.room(server, roomCode)
                    .map(CompactMachines::upgradeAccessor);

            return room.orElse(null);
        }));
    }
}
