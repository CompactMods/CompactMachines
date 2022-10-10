package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import net.minecraft.server.level.ServerLevel;

public interface ILevelLoadedUpgradeListener extends RoomUpgrade {

    /**
     * Called when a level is loaded, typically when the server first boots up.
     */
    default void onLevelLoaded(ServerLevel level, IRoomRegistration room) {}

    /**
     * Called when a level is unloaded.
     */
    default void onLevelUnloaded(ServerLevel level, IRoomRegistration room) {}

}
