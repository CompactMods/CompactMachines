package dev.compactmods.machines.upgrades.api.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface TickingRoomUpgradeComponent {

    void tick(RoomInstance instance);
}
