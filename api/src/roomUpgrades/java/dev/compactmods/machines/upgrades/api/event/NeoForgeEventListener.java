package dev.compactmods.machines.upgrades.api.event;

import net.neoforged.bus.api.Event;

import java.util.stream.Stream;

public interface NeoForgeEventListener {

    Stream<NeoForgeEventHandler<? extends Event>> gatherNeoEvents();
}
