package dev.compactmods.machines.upgrades.api.event.level;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface LevelLoadedUpgradeEventListener extends RoomUpgradeComponentEvent {

    /**
     * Called when a level is loaded, typically when the server first boots up.
     */
    @Override
    void handle(RoomInstance instance);
}
