package dev.compactmods.machines.room.upgrade.event;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventHandler;
import dev.compactmods.machines.api.room.upgrade.event.NeoForgeEventListener;
import net.neoforged.bus.api.Event;

import java.util.stream.Collectors;

public record NeoForgeEventProcessor<TEvt extends Event>(Class<TEvt> type) {

    public void process(TEvt event) {
        final var allRoomInstances = CompactMachines.roomRegistrar()
                .allRooms()
                .collect(Collectors.toSet());

        for (var instance : allRoomInstances) {
            var accessor = instance.getCapability(RoomCapabilities.UPGRADES);

            accessor.all().forEach(inst -> processSingleUpgradeInstance(inst, event));
        }
    }

    private void processSingleUpgradeInstance(RoomUpgradeInstance roomUpgradeInstance, TEvt evt) {
        for (var component : roomUpgradeInstance.upgradeComponents()) {
            if (!(component instanceof NeoForgeEventListener eventListener))
                continue;

            //noinspection unchecked
            eventListener.gatherNeoEvents()
                    .filter(eh -> eh.eventType().equals(type))
                    .map(eh -> (NeoForgeEventHandler<TEvt>) eh)
                    .forEach(eh -> {
                        eh.handle(roomUpgradeInstance, evt);
                    });
        }
    }
}
