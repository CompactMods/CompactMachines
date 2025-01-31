package dev.compactmods.machines.api.room.upgrade.events.level;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface LevelUnloadedUpgradeEventListener extends RoomUpgradeEvent {

    /**
     * Called when a level is unloaded.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
