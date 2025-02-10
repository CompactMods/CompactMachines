package dev.compactmods.machines.api.room.upgrade.event;

import net.neoforged.bus.api.Event;

import java.util.stream.Stream;

public interface NeoForgeEventListener {

    Stream<NeoForgeEventHandler<? extends Event>> gatherNeoEvents();
}
