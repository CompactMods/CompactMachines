package dev.compactmods.machines.upgrades.api.event;

import dev.compactmods.machines.api.room.RoomInstance;
import net.neoforged.bus.api.Event;

public interface NeoForgeEventHandler<TEvt extends Event> {

    Class<TEvt> eventType();

    void handle(RoomInstance instance, TEvt event);
}
