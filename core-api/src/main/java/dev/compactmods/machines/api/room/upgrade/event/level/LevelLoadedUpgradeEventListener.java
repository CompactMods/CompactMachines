package dev.compactmods.machines.api.room.upgrade.event.level;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface LevelLoadedUpgradeEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when a level is loaded, typically when the server first boots up.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
