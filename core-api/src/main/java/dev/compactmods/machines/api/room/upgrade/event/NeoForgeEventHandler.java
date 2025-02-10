package dev.compactmods.machines.api.room.upgrade.event;

import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import net.neoforged.bus.api.Event;

public interface NeoForgeEventHandler<TEvt extends Event> {

    Class<TEvt> eventType();

    void handle(RoomUpgradeInstance instance, TEvt event);
}
