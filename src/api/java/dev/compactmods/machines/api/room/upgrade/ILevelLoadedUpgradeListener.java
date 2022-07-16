package dev.compactmods.machines.api.room.upgrade;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public interface ILevelLoadedUpgradeListener extends RoomUpgrade {

    /**
     * Called when a level is loaded, typically when the server first boots up.
     */
    default void onLevelLoaded(ServerLevel level, ChunkPos room) {}

    /**
     * Called when a level is unloaded.
     */
    default void onLevelUnloaded(ServerLevel level, ChunkPos room) {}

}
