package dev.compactmods.machines.upgrades.api.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeRemovedEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when an update is removed from a room.
     */
    @Override
    void handle(RoomInstance instance);
}
