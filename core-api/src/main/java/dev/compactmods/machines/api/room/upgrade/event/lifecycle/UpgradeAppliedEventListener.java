package dev.compactmods.machines.api.room.upgrade.event.lifecycle;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeAppliedEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when an upgrade is first applied to a room.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
