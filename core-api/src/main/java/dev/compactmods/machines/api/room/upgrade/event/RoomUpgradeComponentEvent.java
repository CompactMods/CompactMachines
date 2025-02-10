package dev.compactmods.machines.api.room.upgrade.event;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;

/**
 * Marker interface for all room upgrade events.
 */
@FunctionalInterface
public interface RoomUpgradeComponentEvent {

   void handle(RoomUpgradeInstance instance);
}
