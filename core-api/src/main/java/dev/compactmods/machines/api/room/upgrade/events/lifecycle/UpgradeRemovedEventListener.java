package dev.compactmods.machines.api.room.upgrade.events.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface UpgradeRemovedEventListener extends RoomUpgradeEvent {

    /**
     * Called when an update is removed from a room.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
