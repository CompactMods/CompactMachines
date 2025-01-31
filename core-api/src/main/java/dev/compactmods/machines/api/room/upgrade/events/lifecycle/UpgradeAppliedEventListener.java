package dev.compactmods.machines.api.room.upgrade.events.lifecycle;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.events.RoomUpgradeEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface UpgradeAppliedEventListener extends RoomUpgradeEvent {

    /**
     * Called when an upgrade is first applied to a room.
     */
    @Override
    void handle(RoomUpgradeInstance instance);
}
