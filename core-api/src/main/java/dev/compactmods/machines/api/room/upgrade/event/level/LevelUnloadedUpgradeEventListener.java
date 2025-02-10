package dev.compactmods.machines.api.room.upgrade.event.level;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface LevelUnloadedUpgradeEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when a level is unloaded.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
