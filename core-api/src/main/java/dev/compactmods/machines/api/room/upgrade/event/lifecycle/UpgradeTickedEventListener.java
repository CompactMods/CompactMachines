package dev.compactmods.machines.api.room.upgrade.event.lifecycle;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;

@FunctionalInterface
public interface UpgradeTickedEventListener extends RoomUpgradeComponentEvent {

    @Override
    void handle(RoomUpgradeInstance instance);
}
