package dev.compactmods.machines.api.room.upgrade.event.lifecycle;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeRemovedEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when an update is removed from a room.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
