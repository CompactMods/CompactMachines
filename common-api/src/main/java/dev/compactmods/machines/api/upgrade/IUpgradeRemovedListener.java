package dev.compactmods.machines.api.upgrade;

import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import net.minecraft.server.level.ServerLevel;

public interface IUpgradeRemovedListener extends RoomUpgradeAction {

    /**
     * Called when an update is removed from a room.
     */
    default void onRemoved(ServerLevel level, IRoomRegistration room) {}
}
