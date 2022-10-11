package dev.compactmods.machines.api;

import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import dev.compactmods.machines.api.room.registration.IRoomSpawnLookup;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Supplier;

public interface ICompactMachinesAddon {

    default void prepare() {
        // no-op
    }

    default void afterRegistration(IEventBus bus) {
        // no-op
    }

    default void acceptRoomOwnerLookup(Supplier<IRoomOwnerLookup> ownerLookup) {
        // Implement and store supplier for room owner lookups
    }

    default void acceptRoomSpawnLookup(Supplier<IRoomSpawnLookup> spawnLookup) {
        // Implement and store supplier for spawn lookups
    }
}
