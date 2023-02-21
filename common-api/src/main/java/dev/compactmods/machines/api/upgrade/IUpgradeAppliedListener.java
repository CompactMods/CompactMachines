package dev.compactmods.machines.api.upgrade;

import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import net.minecraft.server.level.ServerLevel;

public interface IUpgradeAppliedListener extends RoomUpgrade {

    /**
     * Called when an upgrade is first applied to a room.
     */
    default void onAdded(ServerLevel level, IRoomRegistration room) {}
}
