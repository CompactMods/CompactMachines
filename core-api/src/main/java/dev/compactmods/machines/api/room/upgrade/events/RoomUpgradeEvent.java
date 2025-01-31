package dev.compactmods.machines.api.room.upgrade.events;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import net.minecraft.server.level.ServerLevel;

/**
 * Marker interface for all room upgrade events.
 */
@FunctionalInterface
public interface RoomUpgradeEvent {

   void handle(RoomUpgradeInstance instance);
}
