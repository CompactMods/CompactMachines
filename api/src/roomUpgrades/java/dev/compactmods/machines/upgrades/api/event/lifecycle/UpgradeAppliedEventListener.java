package dev.compactmods.machines.upgrades.api.event.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeAppliedEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when an upgrade is first applied to a room.
     */
    @Override
    void handle(RoomInstance instance);
}
