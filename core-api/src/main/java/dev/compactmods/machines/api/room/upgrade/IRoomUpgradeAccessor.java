package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.room.RoomInstance;

import java.util.Optional;
import java.util.UUID;

public interface IRoomUpgradeAccessor {

    Optional<RoomUpgradeInstance> getExistingInstance(RoomInstance roomInstance, UUID id);

    RoomUpgradeInstance getOrCreateInstance(RoomInstance roomInstance, UUID id);

    void clearCache();

    void removeInstance(UUID uuid);
}
